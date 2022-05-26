package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public class WorldNode implements DataStorable {
    public double weighting;
    public long lastVisit;

    public boolean isAir;

    public WorldNode(int worldX, int worldY, int worldZ, boolean isAir) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.isAir = isAir;
    }
    public WorldNode(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldNode worldNode = (WorldNode) o;
        return worldX == worldNode.worldX && worldY == worldNode.worldY && worldZ == worldNode.worldZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldX, worldY, worldZ);
    }
    public int worldX;

    public int worldY;
    public int worldZ;

    @Override
    public void write(CompoundTag out) {

    }

    @Override
    public void read(CompoundTag in) {

    }
}
