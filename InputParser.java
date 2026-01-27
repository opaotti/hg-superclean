import org.json.*;
import java.util.*;

public class InputParser {
    private final JSONObject root;

    public InputParser(String line){
        this.root = new JSONObject(line);
        //System.err.println(root);
    }

    public Config parseConfig(){
        JSONObject cfg = root.getJSONObject("config");
        boolean duel = root.optJSONArray("visible_bots") != null;

        return new Config(
                cfg.getInt("width"),
                cfg.getInt("height"),
                cfg.getInt("max_gems"),
                cfg.getInt("vis_radius"),
                cfg.getInt("bot_seed"),
                cfg.getInt("max_ticks"),
                cfg.getInt("signal_radius"),
                cfg.getInt("gem_ttl"),
                duel
        );
    }

    public Point parseBotPos(){
        JSONArray arr = root.getJSONArray("bot");
        return new Point(arr.getInt(0), arr.getInt(1));
    }

    public Point parseEnemyPos(){
        if (!Bot.cfg.duel()) return null;

        JSONArray arr = root.optJSONArray("visible_bots");
        if (arr == null) return null;
        if (arr.isEmpty()) return null;

        JSONArray pos = arr.getJSONObject(0).getJSONArray("position");

        Point p = new Point(pos.getInt(0), pos.getInt(1));

        return p;
    }

    public Set<Point> parseWalls() {
        return parsePointArray("wall");
    }

    public Set<Point> parseFloors() {
        return parsePointArray("floor");
    }

    private Set<Point> parsePointArray(String key) {
        Set<Point> result = new HashSet<>();
        JSONArray arr = root.optJSONArray(key);
        if (arr == null) return result;

        for (int i = 0; i < arr.length(); i++) {
            JSONArray p = arr.getJSONArray(i);
            Point pos = new Point(p.getInt(0), p.getInt(1));
            result.add(pos);
        }
        return result;
    }

    public List<Gem> parseVisibleGems() {
        List<Gem> gems = new ArrayList<>();
        JSONArray arr = root.optJSONArray("visible_gems");
        if (arr == null) return gems;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject g = arr.getJSONObject(i);
            JSONArray p = g.getJSONArray("position");

            Point pos = new Point(p.getInt(0), p.getInt(1));

            if (pos.equals(new Point(9, 1))) System.err.println("Gem gesehen.");

            gems.add(new Gem(
                    pos,
                    g.getInt("ttl")
            ));
        }
        return gems;
    }

    public int parseTick() {
        return root.getInt("tick");
    }

    public double parseSignal() { return root.getDouble("signal_level"); }
}
