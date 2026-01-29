import java.util.*;

public class GemPredictionTree extends Logger {
    private static List<GemPredictionTree> tree = new ArrayList<>();
    public final Point pos;
    public GemPredictionTree parent;
    public List<GemPredictionTree> children = new ArrayList<>();

    public GemPredictionTree(Point pos) {
        this.pos = pos;
    }

    public void addChild(Point childPos) {
        if(getChildrenPos().contains(childPos)) {
            return;
        }

        GemPredictionTree child = new GemPredictionTree(childPos);
        child.parent = this;
        children.add(child);
    }

    private void addNode(GemPredictionTree node){
        if (!children.contains(node)) {
            node.parent = this;
            children.add(node);
        }
    }

    public void addChildren(Collection<Point> points){
        List<Point> list = points.stream().sorted(Comparator.comparingInt(Utils::rotatedOrder)).toList();
        Set<Point> existing = new HashSet<>();
        for (GemPredictionTree node : tree) existing.add(node.pos);

        boolean added = false;

        for (Point p : list) {
            if(existing.contains(p)) continue;
            addChild(p);
            added = true;
        }
        if (!added && children.isEmpty() && parent != null) parent.removeChildNode(this);
    }

    public int checkGemLayer(Point botPos){
        int depth = searchDepthOfPoint(botPos);
        if (depth != -1) {
            log(depth+". Layer soll entfernt werden wegen gem");
            removeLayer(depth, botPos);
            return depth;
        }
        return 0;
    }

    public int searchDepthOfPoint(Point pos) {
        Queue<Map.Entry<GemPredictionTree, Integer>> queue = new LinkedList<>();

        queue.add(Map.entry(this, 0));

        while (!queue.isEmpty()) {
            Map.Entry<GemPredictionTree, Integer> entry = queue.poll();
            GemPredictionTree current = entry.getKey();
            int depth = entry.getValue();

            if (current.pos != null && current.pos.equals(pos)) {
                return depth;
            }

            for (GemPredictionTree child : current.children){
                queue.add(Map.entry(child, depth+1));
            }
        }
        return -1;
    }

    public void removeLayer(int targetDepth, Point botPos) {
        removeLayerRecursive(this, 0, targetDepth, botPos);
    }


    private void removeLayerRecursive(
            GemPredictionTree node,
            int currentDepth,
            int targetDepth,
            Point botPos
    ) {
        if (currentDepth + 1 == targetDepth) {
            List<GemPredictionTree> toAdd = new ArrayList<>();

            for (GemPredictionTree child : node.children){
                // Kinder zwischenspeichern
                for (GemPredictionTree grandChild : child.children) {
                    if (grandChild.pos.equals(botPos)) continue;
                    grandChild.parent = node;
                    toAdd.add(grandChild);
                }
            }

            node.children.clear();
            // NACH der Iteration neue Kinder hinzufügen
            for(GemPredictionTree g : toAdd) node.addNode(g);
            return;
        }

        // Rekursiv weiter nach unten
        for (GemPredictionTree child : node.children) {
            removeLayerRecursive(child, currentDepth + 1, targetDepth, botPos);
        }
    }

    public Set<GemPredictionTree> getDeepestNodes(){
        Set<GemPredictionTree> result = new HashSet<>();
        collectDeepest(this, 0, result, getTreeDepth());
        return result;
    }

    private void collectDeepest(GemPredictionTree node, int depth, Set<GemPredictionTree> result, int maxDepth) {
        if (node.children.isEmpty()) {
            if (depth > maxDepth) {
                maxDepth = depth;
                result.clear();
            }
            if (depth == maxDepth && node.pos != null) {
                result.add(node);
            }
            return;
        }

        for (GemPredictionTree child : node.children) {
            collectDeepest(child, depth + 1, result, maxDepth);
        }
    }

    public int getTreeDepth(){
        if (children.isEmpty()) return 0;
        return children.get(0).getTreeDepth()+1;
    }

    public int getOwnDepth(){
        GemPredictionTree current = this;
        int depth = 0;

        while(current.parent != null){
            current = current.parent;
            depth++;
        }
        return depth;
    }

    public boolean isLine(){
        if (children.isEmpty()) return true;
        if (children.size() > 1) return false;
        return children.get(0).isLine();
    }

    public List<Point> getLine() {
        List<Point> line = new ArrayList<>();
        collectLine(this, line);
        return line;
    }

    private void collectLine(GemPredictionTree node, List<Point> line) {
        if (node.pos != null) {
            line.add(node.pos);
        }

        if (!node.children.isEmpty()) {
            collectLine(node.children.get(0), line);
        }
    }

    public List<Point> getChildrenPos(){
        List<Point> l = new ArrayList<>();

        for (GemPredictionTree node : children) l.add(node.pos);
        return l;
    }

    public void removeChildrenPos(Point p){
        Iterator<GemPredictionTree> it = children.iterator();

        while (it.hasNext()){
            GemPredictionTree node = it.next();
            if (node.pos.equals(p)) it.remove();
        }
    }

    public void removeChildNode(GemPredictionTree child){
        children.remove(child);
        if (children.isEmpty() && parent != null) parent.removeChildNode(this);
    }

    public void removeChildrenNodes(Collection<GemPredictionTree> childs){
        for (GemPredictionTree child : childs) removeChildNode(child);
    }

    public GemPredictionTree getChild(){
        if (children.isEmpty()) return null;
        return children.get(0);
    }

    public List<GemPredictionTree> getAllNodes(){
        List<GemPredictionTree> l = new ArrayList<>();
        collect(l);
        return l;
    }

    public void collect(List<GemPredictionTree> l){
        l.add(this);
        for (GemPredictionTree g : children) g.collect(l);
    }

    public List<Point> getPathPositions(){
        GemPredictionTree current = this;
        List<Point> points = new ArrayList<>();

        while(current.parent != null){
            points.add(current.pos);
            current = current.parent;
        }
        return points;
    }

    public boolean fixLayer(int depth, Point fixed){
        Iterator<GemPredictionTree> it = children.iterator();

        while (it.hasNext()) {
            GemPredictionTree child = it.next();

            if (depth == 1) {
                if (!child.pos.equals(fixed)) {
                    it.remove();
                    continue;
                }
            }

            if (depth > 1) {
                boolean keep = child.fixLayer(depth - 1, fixed);
                if (!keep) it.remove();
            }
        }

        return !children.isEmpty();
    }

    public GemPredictionTree getRoot(){
        return pos == null ? this : parent.getRoot();
    }

    public void printTree() {
        if (!mustLog) return;
        printTree("", true);
    }

    private void printTree(String prefix, boolean isLast) {
        if (parent == null) {
            log("ROOT");
        } else {
            log(prefix + (isLast ? "└─ " : "├─ ") + pos);
        }

        for (int i = 0; i < children.size(); i++) {
            children.get(i).printTree(
                    prefix + (parent == null ? "" : (isLast ? "····" : "│···")),
                    i == children.size() - 1
            );
        }
    }

    public static void linkAllNodesList(List<GemPredictionTree> l){
        tree = l;
    }
}
