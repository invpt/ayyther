package net.lovector.ayyther.generator;

public class Coords2D {
    public final int x;
    public final int z;

    public Coords2D(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Coords2D left() {
        return new Coords2D(x - 1, z);
    }

    public Coords2D right() {
        return new Coords2D(x + 1, z);
    }

    public Coords2D above() {
        return new Coords2D(x, z - 1);
    }

    public Coords2D below() {
        return new Coords2D(x, z + 1);
    }

    public Coords2D upperLeft() {
        return new Coords2D(x - 1, z - 1);
    }

    public Coords2D upperRight() {
        return new Coords2D(x + 1, z - 1);
    }

    public Coords2D lowerLeft() {
        return new Coords2D(x - 1, z + 1);
    }

    public Coords2D lowerRight() {
        return new Coords2D(x + 1, z + 1);
    }

    public Coords2D[] surrounding() {
        Coords2D[] surrounding = { left(), right(), above(), below(), upperLeft(), upperRight(), lowerLeft(),
                lowerRight() };

        return surrounding;
    }

    @Override
    public int hashCode() {
        return (x + z) * (x + z + 1) / 2 + x;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coords2D other = (Coords2D) obj;
        if (x != other.x)
            return false;
        if (z != other.z)
            return false;
        return true;
    }
}