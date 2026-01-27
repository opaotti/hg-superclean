import java.util.*;
import org.json.*;

public class Highlighter {
    private Map<String, List<Point>> tiles = new HashMap<>();
    private final boolean TEST = Bot.mustLog;

    public JSONObject drawTiles(){
        JSONObject result = new JSONObject();
        if (!TEST) return result;

        JSONArray highlight = new JSONArray();

        for (Map.Entry<String, List<Point>> entry : tiles.entrySet()){
            List<Point> points = entry.getValue();
            String color = entry.getKey();

            for (Point p : points){
                if(p == null) continue;
                JSONArray tile = new JSONArray();

                tile.put(p.x());
                tile.put(p.y());
                tile.put(color);

                highlight.put(tile);
            }
        }

        result.put("highlight", highlight);
        return result;
    }

    public void addTile(Point p, String color){
        if (!TEST) return;

        List<Point> points = tiles.getOrDefault(color, new ArrayList<>());
        points.add(p);

        tiles.put(color, points);
    }

    public void addTiles(Collection<Point> points, String color){
        if (!TEST) return;

        List<Point> oldPoints = tiles.getOrDefault(color, new ArrayList<>());
        oldPoints.addAll(points);

        tiles.put(color, oldPoints);
    }

    public void clearTiles(){
        tiles.clear();
    }
}
