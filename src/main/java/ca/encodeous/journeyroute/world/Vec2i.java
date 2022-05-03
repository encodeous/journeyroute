package ca.encodeous.journeyroute.world;

import java.util.Objects;

public class Vec2i {
    public Vec2i(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x, z;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2i vec2i = (Vec2i) o;
        return x == vec2i.x && z == vec2i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
