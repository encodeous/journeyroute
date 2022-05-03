package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;

public class RouteWorld implements DataStorable {
    public HashMap<Vec2i, RouteChunk> ChunkMap;
    public boolean hasNode(Vec3i pos){
        var chunk = getChunk(getChunkPosAt(pos));
        return chunk.NodeMap.containsKey(pos);
    }
    public RouteNode getNode(Vec3i pos){
        var chunk = getChunk(getChunkPosAt(pos));
        if(!chunk.NodeMap.containsKey(pos)){
            var nn = new RouteNode(pos.getX(), pos.getY(), pos.getZ());
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

    public RouteChunk getChunk(Vec2i chunk){
        if(!ChunkMap.containsKey(chunk)){
            var nc = new RouteChunk(chunk.x, chunk.z);
            ChunkMap.put(chunk, nc);
            return nc;
        }
        return ChunkMap.get(chunk);
    }

    public RouteWorld(){

    }

    @Override
    public void write(NbtCompound out) {

    }

    @Override
    public void read(NbtCompound in) {
        ChunkMap = new HashMap<>();
    }
}
