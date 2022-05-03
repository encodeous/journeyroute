package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.NbtCompound;

public class RouteNode implements DataStorable {
    public double weighting;
    public long lastVisit;

    public RouteNode(int worldX, int worldY, int worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
    }
    public RouteNode(){

    }

    public int worldX;
    public int worldY;
    public int worldZ;

    @Override
    public void write(NbtCompound out) {

    }

    @Override
    public void read(NbtCompound in) {

    }
}
