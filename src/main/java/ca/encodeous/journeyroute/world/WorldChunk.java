package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Objects;

public class WorldChunk implements DataStorable {
    public int chunkX, chunkZ;
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
