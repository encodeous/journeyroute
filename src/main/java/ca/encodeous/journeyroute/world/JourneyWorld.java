package ca.encodeous.journeyroute.world;

import ca.encodeous.journeyroute.Config;
import ca.encodeous.journeyroute.utils.WorldUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * A class that represents a simplified view of a minecraft world
 */
public class JourneyWorld implements DataStorable {
    public JourneyWorld() {
        ChunkMap = new HashMap<>();
    }

    public HashMap<Vec2i, WorldChunk> ChunkMap;
    public boolean hasNode(Vec3i pos){
        var chunk = getChunk(getChunkPosAt(pos));
        return chunk.NodeMap.containsKey(pos);
    }
    public WorldNode getNode(Vec3i pos){
        var chunk = getChunk(getChunkPosAt(pos));
        if(!chunk.NodeMap.containsKey(pos)){
            var nn = new WorldNode(pos.getX(), pos.getY(), pos.getZ(), true);
            chunk.NodeMap.put(pos, nn);
            return nn;
        }
        return chunk.NodeMap.get(pos);
    }

    public Vec2i getChunkPosAt(Vec3i pos){
        // note: >> 4 is not equivalent to / 16 for negative numbers
        return new Vec2i(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public boolean hasChunk(Vec2i chunk){
        return ChunkMap.containsKey(chunk);
    }

    public WorldChunk getChunk(Vec2i chunk){
        if(!ChunkMap.containsKey(chunk)){
            var nc = new WorldChunk(chunk.x, chunk.z);
            ChunkMap.put(chunk, nc);
            return nc;
        }
        return ChunkMap.get(chunk);
    }

    @Override
    public void write(ByteBuf out) {
        var nodes = ChunkMap.values().stream().toList();
        out.writeInt(nodes.size());
        for(int i = 0; i < nodes.size(); i++){
            nodes.get(i).write(out);
        }
    }

    @Override
    public void read(ByteBuf in) {
        int len = in.readInt();
        ChunkMap = new HashMap<>(len);
        for(int i = 0; i < len; i++){
            var nn = new WorldChunk();
            nn.read(in);
            ChunkMap.put(new Vec2i(nn.chunkX, nn.chunkZ), nn);
        }
    }
}
