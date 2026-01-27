import java.util.*;

public class GemPredictionTree extends Logger {
    public final Point pos;
    public GemPredictionTree parent;
    public List<GemPredictionTree> children = new ArrayList<>();

    public GemPredictionTree(Point pos) {
        this.pos = pos;
    }

    public GemPredictionTree addChild(Point childPos) {
        if(getChildrenPos().contains(childPos)) {
            if (childPos.equals(new Point(17, 26))) log("ES WIRD GESAGT ES IST SCHON CHILD");
            return null;
        }

        GemPredictionTree child = new GemPredictionTree(childPos);
        child.parent = this;
        children.add(child);
        return child;
    }

    private void addNode(GemPredictionTree node){
        if (!children.contains(node)) {
            node.parent = this;
            children.add(node);
        }
    }

    public void addChildren(Collection<Point> points){
        List<Point> list = points.stream().sorted(Comparator.comparingInt(Utils::rotatedOrder)).toList();
        for (Point p : list) {
            if (p.equals(pos)) continue;
            addChild(p);
        }
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
            Iterator<GemPredictionTree> it = node.children.iterator();
            List<GemPredictionTree> toAdd = new ArrayList<>();

            while (it.hasNext()) {
                GemPredictionTree child = it.next();

                // Kinder zwischenspeichern
                for (GemPredictionTree grandChild : child.children) {
                    if (grandChild.pos.equals(botPos)) continue;
                    grandChild.parent = node;
                    toAdd.add(grandChild);
                }

                it.remove();
            }

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
        collectDeepest(this, 0, result, getDepth());
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

    public int getDepth(){
        if (children.isEmpty()) return 0;
        return children.get(0).getDepth()+1;
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

    public void getAll(List<Point> list){
        list.add(pos);
        for(GemPredictionTree child : children) child.getAll(list);
    }

    public GemPredictionTree getChild(){
        if (children.isEmpty()) return null;
        return children.get(0);
    }
}
