package net.lovector.ayyther.utils;

public class Pos2D {
    public final int x;
    public final int y;

    public Pos2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pos2D left() {
        return new Pos2D(x - 1, y);
    }

    public Pos2D right() {
        return new Pos2D(x + 1, y);
    }

    public Pos2D above() {
        return new Pos2D(x, y - 1);
    }

    public Pos2D below() {
        return new Pos2D(x, y + 1);
    }

    public Pos2D upperLeft() {
        return new Pos2D(x - 1, y - 1);
    }

    public Pos2D upperRight() {
        return new Pos2D(x + 1, y - 1);
    }

    public Pos2D lowerLeft() {
        return new Pos2D(x - 1, y + 1);
    }

    public Pos2D lowerRight() {
        return new Pos2D(x + 1, y + 1);
    }

    public Pos2D[] surrounding() {
        Pos2D[] surrounding = { left(), right(), above(), below(), upperLeft(), upperRight(), lowerLeft(),
                lowerRight() };

        return surrounding;
    }

    @Override
    public int hashCode() {
        // TODO: better hashCode() function?
        return (x + y) * (x + y + 1) / 2 + x;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Pos2D && ((Pos2D) obj).x == x && ((Pos2D) obj).y == y);
    }
}