import org.json.JSONObject;

import java.util.*;
import java.io.*;

public class Bot extends Logger {
    // --- Felder ---
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_FLOOR   = 0;
    public static final int TYPE_WALL    = 1;

    public static int currenttick = 0;
    public static Point botPos;
    private static Point bestPOVpos;
    private static int bestPOVSize = 0;
    public static boolean lostControl = false;
    public static final Set<Point> POV = new HashSet<>();

    public static InputParser parser;
    public static Config cfg;
    public static Pathfinder pf;
    public static MapManager mapM;
    public static SignalManager sm;
    public static GridMap map;
    public static GemManager gm;
    public static Highlighter hl;

    // --- Main ---
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        boolean firstTick = true;
        currenttick = 0;
        String line;

        while ((line = br.readLine()) != null) {
            parser = new InputParser(line);

            if (firstTick) firstTick();

            update();

            String move = calculateNextMove();

            if (currenttick == cfg.maxTicks()-1) lastTick();

            System.out.println(move + " " + hl.drawTiles());
            System.out.flush();
            firstTick = false;
        }
    }

    private static void firstTick(){
        cfg = parser.parseConfig();
        log(cfg);

        mapM = new MapManager(cfg.width(), cfg.height());
        map = mapM.getMap();
        pf = new Pathfinder(map);
        gm = new GemManager(pf);
        sm = new SignalManager(map, gm);
        hl = new Highlighter();
    }

    // wird jeden Tick aufgerufen
    private static void update(){
        botPos = parser.parseBotPos();
        currenttick = parser.parseTick();
        log("-----"+currenttick+"-----");
        log("BotPos: "+botPos);
        hl.clearTiles();
        POV.clear();
        if (lostControl && parser.parseSignal() == 0) lostControl = false;

        POV.addAll(parser.parseFloors());
        if (POV.size() > bestPOVSize) {
            bestPOVpos = botPos;
            bestPOVSize = POV.size();
        }

        gm.gems_init(parser.parseVisibleGems());
        mapM.addPOV(POV, parser.parseWalls());
        if(!lostControl) {
            sm.update((float) parser.parseSignal());
        } else log("kontrolle verloren...");
        gm.update(botPos);

        if (mustLog){
            hl.addTiles(sm.getTree(), "#7ee8fdcc");
            hl.addTiles(mapM.wallPos, "#ff0000cc");
        }
    }

    private static String calculateNextMove(){
        boolean gemPossible = true;
        log("Gems: "+gm.getGems().size());

        if (!gm.getGems().isEmpty()){
            Path path = gm.getBestGemPath();

            log("Gempath...");
            // für den fall, dass alle gems wegen ttl unerreichbar sind
            if (path != null) return path.nextMove();
            gemPossible = false;
        }

        if (parser.parseSignal() > 0 && gemPossible && !lostControl){
            Point bestPotential = sm.getBestPossibleGem(botPos);

            if (bestPotential == null) return "WAIT";

            String move = pf.aStar(botPos, bestPotential).nextMove();

            while(move.equals(Pathfinder.IMPOSSIBLEPATH_STRING)){
                mapM.setWall(bestPotential);

                bestPotential = sm.updatePotential(botPos, bestPotential);
                move = pf.aStar(botPos, bestPotential).nextMove();
            }

            log("Signalmove...");
            return move;
        }

        if (!mapM.mapComplete()){
            Point nearestFrontier = mapM.nearestFrontier(botPos);

            if(nearestFrontier == null){
                mapM.fillUnknownWithWalls();
                return calculateNextMove();
            }

            log("Exploration...");
            return pf.aStar(botPos, nearestFrontier).nextMove();
        }

        if (bestPOVpos != null) {
            log("Best POV Pos...");
            return pf.aStar(botPos, bestPOVpos).nextMove();
        }

        return "S";
    }

    private static void lastTick(){
        log(("A* Calls: "+pf.aStarCalls));
    }

    public static void lostControl(){
        System.err.println("Kontrolle verloren");
        lostControl = true;
        sm.resetRoot();
    }
}