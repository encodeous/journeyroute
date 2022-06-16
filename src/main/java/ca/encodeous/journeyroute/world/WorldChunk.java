package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Objects;

/**
 * A class that represents a 16x16x(world height) section of a world.
 */
public class WorldChunk implements DataStorable {
    /**
     * The chunk coordinate
     */
    public int chunkX, chunkZ;
    /**
     * A map containing all of the blocks in the chunk
     */
    public HashMap<Vec3i, WorldNode> NodeMap;
    public WorldChunk(int x, int z){
        chunkX = x;
        chunkZ = z;
        NodeMap = new HashMap<>();
    }
    public WorldChunk(){

    }
    @Override
    public void write(ByteBuf out) {
        out.writeInt(chunkX);
        out.writeInt(chunkZ);
        out.writeInt(NodeMap.size());
        for (var v : NodeMap.values()) {
            v.write(out);
        }
    }

    @Override
    public void read(ByteBuf in) {
        chunkX = in.readInt();
        chunkZ = in.readInt();
        int len = in.readInt();
        NodeMap = new HashMap<>(len);
        for(int i = 0; i < len; i++){
            var nn = new WorldNode();
            nn.read(in);
            NodeMap.put(new Vec3i(nn.worldX, nn.worldY, nn.worldZ), nn);
        }
    }
}
