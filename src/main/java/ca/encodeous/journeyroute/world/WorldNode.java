package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class WorldNode implements DataStorable {
    public double weighting;
    public long lastVisit;

    public boolean isAir;
    public String metadata = "";

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
    public void write(ByteBuf out) {
        out.writeDouble(weighting);
        out.writeLong(lastVisit);
        out.writeBoolean(isAir);
        var strBytes = metadata.getBytes(StandardCharsets.UTF_8);
        out.writeInt(strBytes.length);
        out.writeBytes(strBytes);
        out.writeInt(worldX);
        out.writeInt(worldY);
        out.writeInt(worldZ);
    }

    @Override
    public void read(ByteBuf in) {
        weighting = in.readDouble();
        lastVisit = in.readLong();
        isAir = in.readBoolean();
        var metaBytes = in.readInt();
        byte[] bytes = new byte[metaBytes];
        in.readBytes(bytes);
        metadata = new String(bytes);
        worldX = in.readInt();
        worldY = in.readInt();
        worldZ = in.readInt();
    }
}
