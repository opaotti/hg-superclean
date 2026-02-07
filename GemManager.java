import java.util.*;

public class GemManager extends Logger {
    private final List<Gem> gems = new ArrayList<>();
    private final Map<Path, Integer> gemPaths = new HashMap<>();
    private final List<Gem> toDeleteGems = new ArrayList<>();
    private Point botPos;
    private boolean mapComplete;
    private Point newSpawntGem;

    private final Pathfinder pf;
    private final int MAX_TTL = Bot.cfg.gem_ttl();

    public GemManager(Pathfinder p){
        pf = p;
    }

    public void update(Point bPos, List<Gem> parsedGems){
        botPos = bPos;
        mapComplete = Bot.mapM.mapComplete();

        gems_init(parsedGems);

        generateGemPaths();
    }

    public Path getBestGemPath(){
        Path best = null;
        int max = 0;

        for (Map.Entry<Path, Integer> entry : gemPaths.entrySet()){
            int score = entry.getValue();
            if (score > max){
                best = entry.getKey();
                max = score;
            }
        }

        if (best == null){
            for (Gem g : gems){
                log(g+" dist zu bot: "+Utils.aStarDistance(botPos, g.pos));
            }
        }

        return best;
    }

    private void generateGemPaths(){
        gemPaths.clear();
        Map<Point, Integer> openGems = new LinkedHashMap<>();

        for (Gem g : gems) openGems.put(g.pos, g.ttl);
        setGemPaths(botPos, openGems, null, 0, true);
    }

    private void setGemPaths(Point currentPos, Map<Point, Integer> openGems, Path path, int score, boolean first){
        if (openGems.isEmpty()){
            gemPaths.put(path, score);
            return;
        }
        for (Map.Entry<Point, Integer> entry : openGems.entrySet()){
            Point pos = entry.getKey();
            if (pos.equals(currentPos)) continue;

            Path pathPiece = pf.aStar(currentPos, pos, mapComplete);
            if (pathPiece.path() == null){
                log("unmöglicher gem dabei");
                Bot.lostControl();
                return;
            }
            int gainedScore = entry.getValue() - pathPiece.len();

            if (gainedScore < 1){
                if (openGems.size() == 1){
                    gemPaths.put(path, score);
                    return;
                }
                continue;
            }

            Map<Point, Integer> newOpenGems = updateTtl(openGems, pathPiece.len());
            newOpenGems.remove(entry.getKey());

            Path newPath;
            if (first) newPath = pathPiece.copy();
            else {
                newPath = path.copy();
                newPath.append(pathPiece);
            }

            setGemPaths(pos, newOpenGems, newPath, score+gainedScore, false);
        }
    }

    private Map<Point, Integer> updateTtl(Map<Point, Integer> openGems, int ticksTraveled){
        Map<Point, Integer> newMap = new LinkedHashMap<>(openGems);

        for (Map.Entry<Point, Integer> entry : openGems.entrySet()){
            int newTtl = entry.getValue() - ticksTraveled;
            if (newTtl > 0) newMap.put(entry.getKey(), newTtl);
        }
        return  newMap;
    }

    // --- Gem-Verarbeitung ---
    private void readVisibleGems(List<Gem> parsedGems){
        newSpawntGem = null;
        Set<Gem> set = new HashSet<>(parsedGems);

        for (Gem g : set) if(!Utils.isPosInGems(g.pos, gems)) {
            gems.add(g);
            log(g.pos+" als neuer Gem");
            if (g.ttl == MAX_TTL) newSpawntGem = g.pos;
        }
    }

    private void gemsTick() {
        Iterator<Gem> it = gems.iterator();

        while (it.hasNext()) {
            Gem g = it.next();
            g.tick();

            if (g.ttl < 0) {
                log("Gem an "+g.pos+" wegen ttl entfernt ➖");
                toDeleteGems.add(new Gem(g.pos, 0));
                it.remove();
            }
        }
    }

    public void gems_init(List<Gem> parsedGems){
        readVisibleGems(parsedGems);
        log("Gems: "+gems);
        toDeleteGems.clear();

        if(!gems.isEmpty()) for(Gem g : gems) if(g.pos.equals(botPos) ||
                // damit falls ein enemy die nimmt die entfernt werden.
                (Bot.POV.contains(g.pos) && !Utils.isPosInGems(g.pos, parsedGems))) {
            log("➖ Gem gesammelt bei "+g.pos);
            toDeleteGems.add(g);
        }
        if (!toDeleteGems.isEmpty()) {
            gems.removeAll(toDeleteGems);
        }

        gemsTick();
    }

    public void insertGemsFromLists(List<Point> points, List<Integer> ttls){
        if (points.isEmpty() || ttls.isEmpty() || Bot.lostControl || points.size() != ttls.size()) return;
        gems.clear();

        for(int c = 0; c < points.size(); c++){
            Gem g = new Gem(points.get(c), ttls.get(c)-1);
            gems.add(g);
        }
        log("inserted, gems: "+gems);
    }

    public List<Gem> getGems(){
        return gems;
    }

    public Point newSpawnedGemPos(){
        return newSpawntGem;
    }

    public List<Gem> getToDeleteGems(){
        return toDeleteGems;
    }
}
