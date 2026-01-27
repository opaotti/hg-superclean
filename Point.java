import java.util.*;

public record Point(int x, int y) {
    public static Point stringToPoint(String s){
        String[] parts = s.split(", ");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        return new  Point(x, y);
    }

    public List<Point> neighbours(){
        return List.of(
                new Point(x+1, y),
                new Point(x-1, y),
                new Point(x, y+1),
                new Point(x, y-1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
