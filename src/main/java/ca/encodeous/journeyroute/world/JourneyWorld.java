package ca.encodeous.journeyroute.world;

import ca.encodeous.journeyroute.Config;
import ca.encodeous.journeyroute.utils.WorldUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.PriorityQueue;

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
    public void write(CompoundTag out) {

    }

    @Override
    public void read(CompoundTag in) {
        ChunkMap = new HashMap<>();
    }



    public Route getRouteTo(Vec3i cur, Vec3i dest){
        if(!hasNode(cur) || !hasNode(dest)) return null;
        var startNode = getNode(cur);
        var dist = new HashMap<Vec3i, Double>();
        var prev = new HashMap<Vec3i, Vec3i>();
        var nodes = new PriorityQueue<RouteNode>();
        var srn = new RouteNode(0, 0, startNode);
        nodes.add(srn);
        var route = new Route();
        dist.put(srn.pos, 0d);
        while(!nodes.isEmpty()){
            var v = nodes.poll();
            if(v.pos.equals(dest)) break;
//            route.Path.add(v.pos);
            double w = v.weight;
            if(dist.getOrDefault(v.pos, 1e17) < w) continue;
            for(var block : WorldUtils.getTraversableBlocks(this, v.pos, 1)){
                double heurWeight = cur.distSqr(dest);
                var nPos = new Vec3i(block.worldX, block.worldY, block.worldZ);
                double newWeight = w + Math.sqrt(v.pos.distSqr(nPos)) + block.weighting;
                if(newWeight < dist.getOrDefault(nPos, 1e17)){
                    dist.put(nPos, newWeight);
                    prev.put(nPos, v.pos);
                    nodes.add(new RouteNode(heurWeight + newWeight, newWeight, block));
                }
            }

            for(var block : WorldUtils.getSurroundingAir(this, v.pos, 1)){
                double heurWeight = cur.distSqr(dest) + Config.AIR_WEIGHT;
                var nPos = new Vec3i(block.worldX, block.worldY, block.worldZ);
                double newWeight = w + Math.sqrt(v.pos.distSqr(nPos)) + block.weighting + Config.AIR_WEIGHT;
                if(newWeight < dist.getOrDefault(nPos, 1e17)){
                    dist.put(nPos, newWeight);
                    prev.put(nPos, v.pos);
                    nodes.add(new RouteNode(heurWeight + newWeight, newWeight, block));
                }
            }
        }
        Vec3i prevNode = dest;
        while(prevNode != null){
            route.Path.addFirst(prevNode);
            prevNode = prev.getOrDefault(prevNode, null);
        }
        return route;
    }
}
