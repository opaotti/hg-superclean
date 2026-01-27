import java.util.*;

public class MapManager {
    private GridMap map;
    private Pathfinder pf;

    private final Set<Point> unknownPos = new HashSet<>();
    public final Set<Point> wallPos = new HashSet<>();/*
    private final Set<Point> floorPos = new HashSet<>();*/

    public MapManager(int w, int h){
        map = new GridMap(w, h);

        unknownPos.addAll(map.getUnknown());
    }

    public GridMap getMap(){
        return map;
    }

    public void addPOV(Set<Point> floors, Set<Point> walls){
        unknownPos.removeAll(floors);
        unknownPos.removeAll(walls);
        wallPos.addAll(walls);

        for (Point wall : walls) map.setWall(wall);
        for (Point floor : floors) map.setFloor(floor);
    }

    public boolean mapComplete(){
        return unknownPos.isEmpty();
    }

    public Point nearestFrontier(Point startPos){
        List<Point> frontiers = new ArrayList<>();
        for (Point p : unknownPos) if(map.isFrontier(p)) frontiers.add(p);

        Bot.hl.addTiles(frontiers, "#884e6ccc");

        return Utils.getNearest(startPos, frontiers);
    }

    public void fillUnknownWithWalls(){
        unknownPos.clear();
        map.fillUnknownToWalls();
    }

    public void setWall(Point p){
        map.setWall(p);
        wallPos.add(p);
        unknownPos.remove(p);
    }
}
