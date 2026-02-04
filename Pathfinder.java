import java.util.*;

public class Pathfinder {
    private GridMap map;

    private Map<AStarKey, Path> aStarCache = new HashMap<>();
    public int aStarCalls = 0;

    public static final String IMPOSSIBLEPATH_STRING = "o";
    private static final Path IMPOSSIBLEPATH = new Path(IMPOSSIBLEPATH_STRING, null);

    public Pathfinder(GridMap m){
        map = m;
    }

    public Path aStar(Point startPos, Point goalPos) {
        return aStar(startPos, goalPos, true);
    }

    public Path aStar(Point startPos, Point goalPos, boolean cached){
        if(startPos.equals(goalPos)) return new Path("WAIT", new ArrayList<>());

        AStarKey key = new AStarKey(startPos, goalPos);

        if(aStarCache.containsKey(key) && false){
            return aStarCache.get(key);
        }

        aStarCalls++;

        PriorityQueue<CheckedPoint> openQueue =
                new PriorityQueue<>(
                        Comparator.comparingDouble(CheckedPoint::fValue)
                                .thenComparingInt(Utils::rotatedOrderCP)
                );

        Map<Point, CheckedPoint> openMap = new HashMap<>();
        Map<Point, CheckedPoint> closedList = new HashMap<>();

        CheckedPoint start = new CheckedPoint(startPos, 0, 0, null, 0);
        openQueue.add(start);
        openMap.put(startPos, start);

        while(!openQueue.isEmpty()){
            CheckedPoint current = openQueue.poll();
            Point currentPos = current.pos();
            openMap.remove(currentPos);

            if(goalPos.equals(currentPos)) return nextMove(startPos, goalPos, current, cached);

            closedList.put(currentPos, current);

            List<Point> neighbours = map.getNotWallNeighbours(currentPos);

            for (Point p : neighbours) {
                if (closedList.containsKey(p)) continue;
                int isUnknown = map.isUnknown(p) ? 1 : 0;
                int unknownPassed = current.unknownPassed() + isUnknown;

                int g = current.gValue() + 1;
                int h = Utils.getManhattan(p, goalPos);
                float f = g + h + (float)(Math.pow((float) Math.E, (0.1f*unknownPassed)));

                CheckedPoint existing = openMap.get(p);

                if (existing == null) {
                    CheckedPoint cp = new CheckedPoint(p, g, f, current, unknownPassed);
                    openQueue.add(cp);
                    openMap.put(p, cp);
                } else if (g < existing.gValue()) {
                    // Update f-values wenn dieser weg besser ist
                    openQueue.remove(existing);
                    existing = new CheckedPoint(p, g, f, current, unknownPassed);
                    openQueue.add(existing);
                    openMap.put(p, existing);
                }
            }
        }

        return IMPOSSIBLEPATH;
    }

    private Path nextMove(Point startPos, Point goalPos, CheckedPoint cp, boolean cached){
        List<CheckedPoint> pathCP = new ArrayList<>();
        while(cp != null){
            pathCP.add(cp);
            cp = cp.parent();
        }

        Collections.reverse(pathCP);
        Point next = pathCP.get(1).pos();

        String move = Utils.computeMove(startPos, next);

        boolean cachable = cached;
        List<Point> pPath = new ArrayList<>();
        for (CheckedPoint c : pathCP){
            pPath.add(c.pos());
            if (cachable) if (map.isUnknown(c.pos())) cachable = false;
        }
        Path path = new Path(move, pPath);

        if(cached && cachable){
            aStarCache.put(new AStarKey(startPos, goalPos), path);
            aStarCache.put(new AStarKey(goalPos, startPos), path.reversePath());
        }

        return path;
    }
}
