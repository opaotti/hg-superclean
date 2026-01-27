import java.util.*;

public class SignalManager extends Logger {
    private float signal;
    private Point botPos;
    private GridMap map;
    private GemPredictionTree root = new GemPredictionTree(null);
    private List<int[]> ringLookUp = new ArrayList<>();
    private Set<GemPredictionTree> deepestNodes = new HashSet<>();

    public List<Point> gems = new ArrayList<>();
    public List<Integer> gemsTtl = new ArrayList<>();

    private final Map<GemPredictionTree, Float> signalSum = new HashMap<>();
    private static final int SIGNALRADIUS = Bot.cfg.signalRadius();
    private static final int LOOKUP_RINGSIZE = (int)(Utils.getPythagorasDist(new Point(1, 1), new Point(Bot.cfg.width(), Bot.cfg.height())));
    private static final int MAX_TTL = Bot.cfg.gem_ttl();
    private static final int MAX_GEMS = Bot.cfg.maxGems();

    public SignalManager(GridMap m){
        map = m;
        setupRingLookUp();
        log("Lookup size: "+ringLookUp.size());
    }

    public void update(float signalValue){
        signal = signalValue;
        signalSum.clear();
        deepestNodes.clear();
        botPos = Bot.botPos;
        log("Signal: "+signal);

        updateTtl();
        // Um einsammeln zu überprüfen WIP
        int gemDepth = root.checkGemLayer(botPos);
        if(gemDepth > 0){
            gemDepth--;
            gems.remove(gemDepth);
            gemsTtl.remove(gemDepth);
        }

        if (root.isLine()){
            log("Alles berechnet Gem size: "+root.getDepth());
            gems = new ArrayList<>(root.getLine());

            Bot.gm.insertGemsFromLists(gems, gemsTtl);
        }
        else{
            log("root.children.size: "+root.children.size());
            log(root.getChildrenPos());
            if(!root.children.isEmpty()) if (!root.getChild().children.isEmpty()) log("root.grandchild.children.size: "+root.getChild().getChild().children.size());
            log(root.getChild().getChildrenPos());
        }

        if (root.children.isEmpty() && signalValue > 0){
            root.addChildren(euclideanRing(botPos, getDistFromSignal(signalValue)));

            gemsTtl.add(MAX_TTL);
            gems.add(null);
        }
        else if (signalValue > 0){
            deepestNodes.addAll(root.getDeepestNodes());
            computeSignalSums(root, 0f);

            if(pruneAndCheckNewGem()){
                if (gemsTtl.size() > MAX_GEMS){
                    Bot.lostControl();
                    return;
                }

                for (GemPredictionTree node : deepestNodes){
                    float newSignal = signalValue - getSignalFromNode(node);
                    int d = getDistFromSignal(newSignal);
                    if(d > LOOKUP_RINGSIZE) continue;

                    Set<Point> possibleNewGem = euclideanRing(botPos, d);

                    //log("Restsignal: "+newSignal);
                    //log("dist daraus: "+d);
                    //log(possibleNewGem);
                    node.addChildren(possibleNewGem);
                }

                gemsTtl.add(MAX_TTL);
                gems.add(null);
            }
        }
    }

    public Point getBestPossibleGem(Point startPos){
        return Utils.getNearest(startPos, root.getChildrenPos());
    }

    private boolean pruneAndCheckNewGem() {
        final float EPS = 0.0001f;
        boolean explainable = false;
        List<GemPredictionTree> toDelete = new ArrayList<>();

        Iterator<GemPredictionTree> it = deepestNodes.iterator();

        while (it.hasNext()) {
            GemPredictionTree leaf = it.next();
            float expected = signalSum.get(leaf);

            float dif = Math.abs(expected - signal);

            if (dif < EPS) {explainable = true;
            } else {
                // Pfad unmöglich -> entfernen
                toDelete.add(leaf);
            }
        }

        if (!explainable) {log("➕ Neuer Gem MUSS existieren");}
        else{
            for(GemPredictionTree leaf : toDelete){
                removeLeafPath(leaf);
                deepestNodes.remove(leaf);
            }
        }

        return !explainable;
    }

    private void computeSignalSums(GemPredictionTree node, float parentSum){
        float sum = parentSum;

        if (node.pos != null){
            sum += getSignalFromDist(Utils.getPythagorasDist(botPos, node.pos));
        }

        signalSum.put(node, sum);

        for (GemPredictionTree child : node.children){
            computeSignalSums(child, sum);
        }
    }

    private void removeLeafPath(GemPredictionTree leaf) {
        GemPredictionTree current = leaf;

        while (current.parent != null) {
            GemPredictionTree parent = current.parent;

            parent.children.remove(current);

            // wenn Parent noch andere Kinder hat -> abbrechen
            if (!parent.children.isEmpty()) return;

            current = parent;
        }
    }

    private static int getDistFromSignal(float signal){
        if(signal <= 0) return -1;
        return Math.round((float) Math.sqrt((1/signal) -1) * SIGNALRADIUS);
    }

    private static float getSignalFromDist(float dist){
        float x = dist / SIGNALRADIUS;
        return 1/(1+ x*x);
    }

    private float getSignalFromNode(GemPredictionTree node){
        GemPredictionTree current = node;
        float signal = 0;
        while(current.parent != null){
            signal += getSignalFromDist(Utils.getPythagorasDist(botPos, current.pos));
            current = current.parent;
        }
        return signal;
    }

    private Set<Point> euclideanRing(Point pos, int d) {
        if (d < 1 || d > ringLookUp.size()) return new HashSet<>();

        int[] offs = ringLookUp.get(d);
        Set<Point> result = new HashSet<>(offs.length / 2);

        for (int i = 0; i < offs.length; i += 2) {
            int nx = pos.x() + offs[i];
            int ny = pos.y() + offs[i+1];

            if (nx < 0 || ny < 0) continue;

            Point p = new Point(nx, ny);
            if (!map.isWall(p)) {
                result.add(p);
            }
        }
        return result;
    }

    public Point updatePotential(Point startPos, Point wallPos){
        root.removeChildrenPos(wallPos);
        return Utils.getNearest(startPos, root.getChildrenPos());
    }

    private void updateTtl() {
        for (int idx = 0; idx < gemsTtl.size(); idx++) {
            int ttl = gemsTtl.get(idx) - 1;
            gemsTtl.set(idx, ttl);
            log(ttl);

            if (ttl <= 0) {
                gemsTtl.remove(idx);
                gems.remove(idx);
                root.removeLayer(idx+1, botPos);
                idx--; // wichtig wegen Indexverschiebung
            }
        }
    }

    public List<Point> getTree(){
        List<Point> list = new ArrayList<>();
        root.getAll(list);
        return list;
    }

    public void setupRingLookUp(){
        for (int d = 0; d <= LOOKUP_RINGSIZE+1; d++){
            List<Integer> tmp = new ArrayList<>();

            float delta = 0.5001f;
            double min2 = (d - delta) * (d - delta);
            double max2 = (d + delta) * (d + delta);

            for (int dx = -d; dx <= d; dx++) {
                for (int dy = -d; dy <= d; dy++) {
                    double dist2 = dx*dx + dy*dy;
                    if (dist2 < min2 || dist2 > max2) continue;

                    tmp.add(dx);
                    tmp.add(dy);
                }
            }

            int[] arr = new int[tmp.size()];
            for (int i = 0; i < tmp.size(); i++) arr[i] = tmp.get(i);
            ringLookUp.add(arr);
        }
    }
}
