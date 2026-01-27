import java.util.*;
public record Path(String nextMove, List<Point> path) {
    public void append(Path p){
        List<Point> pList = new ArrayList<>(p.path());
        pList.remove(0);

        path.addAll(pList);
    }

    public int len(){
        return path.size();
    }

    public Point getFirst(){
        return path.get(0);
    }

    public Point getLast(){
        return path.get(len()-1);
    }

    public Path copy() {
        return new Path(
                nextMove,
                new ArrayList<>(path)
        );
    }

    public Path reversePath(){
        List<Point> points = new ArrayList<>(path);
        Collections.reverse(points);

        Point next = points.get(1);
        Point start = points.get(0);

        String move = Utils.computeMove(start, next);

        return new Path(move, points);
    }
}
