import java.util.*;

public class Utils {
    private static Pathfinder pf = Bot.pf;
    private static final int SEED = Bot.cfg.seed();

    public static int aStarDistance(Point p1, Point p2, boolean cached){
        if (p1 == null || p2 == null) return -1;
        if (p1.equals(p2)) return -1;
        Path path = pf.aStar(p1, p2, cached);
        if(path.nextMove().equals(Pathfinder.IMPOSSIBLEPATH_STRING)) return -1;

        return path.len();
    }

    public static int aStarDistance(Point p1, Point p2){
        return aStarDistance(p1, p2, true);
    }

    public static Point getNearest(Point pos, Collection<Point> points){
        if(points.isEmpty()) return null;

        Point best = null;
        int min = Integer.MAX_VALUE;

        for(Point p : points){
            int dist = aStarDistance(pos, p);
            if(dist == -1) continue;

            if(dist <= min){
                if (dist == min) if (rotatedOrder(p) > rotatedOrder(pos)) continue;

                best = p;
                min = dist;
            }
        }
        return best;
    }

    public static int getManhattan(Point startPos, Point goalPos) {
        int xdif = Math.abs(startPos.x() - goalPos.x());
        int ydif = Math.abs(startPos.y() - goalPos.y());
        return xdif + ydif;
    }

    public static float getPythagorasDist(Point p1, Point p2){
        int dx = p1.x()- p2.x();
        int dy = p1.y() - p2.y();
        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    public static String computeMove(Point from, Point to) {
        if (to.x() < from.x()) return "W";
        if (to.x() > from.x()) return "E";
        if (to.y() < from.y()) return "N";
        if (to.y() > from.y()) return "S";

        return "WAIT";
    }

    public static boolean isPosInGems(Point pos, Collection<Gem> gems){
        for (Gem g : gems) if (g.pos.equals(pos)) return true;
        return false;
    }

    public static Set<Point> gemsToPointSet(Collection<Gem> gems){
        Set<Point> set = new HashSet<>();

        for (Gem g : gems) set.add(g.pos);
        return set;
    }

    public static int rotatedOrder(Point p) {
        if(p == null) return -1;
        int h = p.x() * 73856093 ^ p.y() * 19349663 ^ SEED * 83492791;
        return h;
    }

    public static int rotatedOrderCP(CheckedPoint p) {
        return rotatedOrder(p.pos());
    }
}
