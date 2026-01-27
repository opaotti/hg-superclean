public class Gem {
    public int ttl;
    public final Point pos;

    public Gem(Point pos, int ttl) {
        this.pos = pos;
        this.ttl = ttl;
    }

    public void tick() {
        ttl--;
    }

    public String toString(){
        return pos+", "+ttl;
    }
}
