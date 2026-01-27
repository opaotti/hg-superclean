import java.util.*;
import java.util.List;

public class GridMap {
    private int[][] map;
    private int width;
    private int height;

    public GridMap(int w, int h){
        width = w;
        height = h;

        createMap();
    }

    private void createMap(){
        map = new int[width][height];

        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                if(h == 0 || h == height-1 || w == 0 ||w == width-1){
                    map[w][h] = Bot.TYPE_WALL;
                    continue;
                }

                map[w][h] = Bot.TYPE_UNKNOWN;
            }
        }
    }

    public void setType(Point p, int type){
        map[p.x()][p.y()] = type;
    }

    public void setFloor(Point p){
        map[p.x()][p.y()] = Bot.TYPE_FLOOR;
    }

    public void setWall(Point p){
        map[p.x()][p.y()] = Bot.TYPE_WALL;
    }

    public boolean isWall(Point p) {
        if (isOutOfBounds(p)) return true;

        return map[p.x()][p.y()] == Bot.TYPE_WALL;
    }

    public boolean isFloor(Point p) {
        return map[p.x()][p.y()] == Bot.TYPE_FLOOR;
    }

    public boolean isUnknown(Point p) {
        return map[p.x()][p.y()] == Bot.TYPE_UNKNOWN;
    }

    public void fillUnknownToWalls(){
        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                if(map[w][h] == Bot.TYPE_UNKNOWN) map[w][h] = Bot.TYPE_WALL;
            }
        }
    }

    public boolean isFrontier(Point pos){
        List<Point> n = pos.neighbours();
        for(Point p : n) if(isFloor(p)) return true;

        return false;
    }

    public Set<Point> getUnknown(){
        Set<Point> set = new HashSet<>();

        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                if(map[w][h] == Bot.TYPE_UNKNOWN) set.add(new Point(w, h));
            }
        }
        return set;
    }

    public List<Point> getFloorNeighbours(Point pos){
        List<Point> l =new ArrayList<>();
        List<Point> n = pos.neighbours();

        for (Point p : n){
            if (isFloor(p)) l.add(p);
        }
        return l;
    }

    public List<Point> getNotWallNeighbours(Point pos){
        List<Point> l =new ArrayList<>();
        List<Point> n = pos.neighbours();

        for (Point p : n){
            if (!isWall(p)) l.add(p);
        }
        return l;
    }

    public int getType(Point p){
        if (isOutOfBounds(p)) return Bot.TYPE_WALL;

        return map[p.x()][p.y()];
    }

    private boolean isOutOfBounds(Point p){
        return p.x() < 0 ||p.x() >= width || p.y() < 0 ||p.y() >= height;
    }
}