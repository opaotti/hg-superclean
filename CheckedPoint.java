public record CheckedPoint(Point pos, int gValue, float fValue, CheckedPoint parent, int unknownPassed) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckedPoint cp)) return false;
        return pos.equals(cp.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
