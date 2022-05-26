package ca.encodeous.journeyroute.utils;

import ca.encodeous.journeyroute.world.JourneyWorld;
import ca.encodeous.journeyroute.world.RouteNode;
import ca.encodeous.journeyroute.world.WorldNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class WorldUtils {
    public static class QueuedBlock {
        public QueuedBlock(BlockPos pos, int dist) {
            this.pos = pos;
            this.dist = dist;
        }

        public BlockPos pos;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueuedBlock that = (QueuedBlock) o;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        public int dist;
    }
    private static final int[] mvarr = {0, 0, 1, -1}, mvarc = {1, -1, 0, 0};
    private static final int[]
            mvarr3 = {0, 0, 0, 0, 1, -1},
            mvarc3 = {0, 0, 1, -1, 0, 0},
            mvarz3 = {1, -1, 0, 0, 0, 0};
    public static HashSet<QueuedBlock> getTraversableBlocks(Level world, Vec3i originPos, int radius){
        var visited = new HashSet<QueuedBlock>();
        if(originPos == null) return visited;
        var dq = new ArrayDeque<QueuedBlock>();
        dq.add(new QueuedBlock(getSurfaceLevelBlock(world, originPos), 0));
        while(!dq.isEmpty()){
            var v = dq.poll();
            if(v.pos == null) continue;
            visited.add(v);
            if(v.dist == radius) continue;
            for(int i = 0; i < 4; i++){
                int nx = mvarr[i] + v.pos.getX();
                int nz = mvarc[i] + v.pos.getZ();
                var bpos = getSurfaceLevelBlock(world, new BlockPos(nx, v.pos.getY(), nz));
                if(bpos == null) continue;
                var cblock = new QueuedBlock(bpos, v.dist + 1);
                if(visited.contains(cblock)) continue;
                visited.add(cblock);
                dq.add(cblock);
            }
        }
        return visited;
    }

    public static List<WorldNode> getTraversableBlocks(JourneyWorld world, Vec3i originPos, int radius){
        var visited = new HashSet<QueuedBlock>();
        if(originPos == null) return Collections.emptyList();
        var dq = new ArrayDeque<QueuedBlock>();
        dq.add(new QueuedBlock(getSurfaceLevelBlock(world, originPos), 0));
        while(!dq.isEmpty()){
            var v = dq.poll();
            if(v.pos == null) continue;
            visited.add(v);
            if(v.dist == radius) continue;
            for(int i = 0; i < 4; i++){
                int nx = mvarr[i] + v.pos.getX();
                int nz = mvarc[i] + v.pos.getZ();
                var bpos = getSurfaceLevelBlock(world, new BlockPos(nx, v.pos.getY(), nz));
                if(bpos == null) continue;
                var cblock = new QueuedBlock(bpos, v.dist + 1);
                if(visited.contains(cblock)) continue;
                visited.add(cblock);
                dq.add(cblock);
            }
        }
        return visited.stream().map(x->world.getNode(x.pos)).toList();
    }

    public static List<QueuedBlock> getSurroundingAir(Level world, Vec3i originPos, int radius){
        var visited = new HashSet<QueuedBlock>();
        if(originPos == null) return Collections.emptyList();
        var dq = new ArrayDeque<QueuedBlock>();
        dq.add(new QueuedBlock(new BlockPos(originPos), 0));
        while(!dq.isEmpty()){
            var v = dq.poll();
            if(v.pos == null) continue;
            visited.add(v);
            if(v.dist >= radius) continue;
            for(int i = 0; i < 6; i++){
                int nx = mvarr3[i] + v.pos.getX();
                int ny = mvarc3[i] + v.pos.getY();
                int nz = mvarz3[i] + v.pos.getZ();
                var bpos = new BlockPos(nx, ny, nz);
                if(!isWalkableThrough(world, bpos)) continue;
                var cblock = new QueuedBlock(bpos, v.dist + 1);
                if(visited.contains(cblock)) continue;
                visited.add(cblock);
                dq.add(cblock);
            }
        }
        return visited.stream().toList();
    }

    public static List<WorldNode> getSurroundingAir(JourneyWorld world, Vec3i originPos, int radius){
        var visited = new HashSet<QueuedBlock>();
        if(originPos == null || !world.hasNode(originPos)) return Collections.emptyList();
        var dq = new ArrayDeque<QueuedBlock>();
        dq.add(new QueuedBlock(new BlockPos(originPos), 0));
        while(!dq.isEmpty()){
            var v = dq.poll();
            if(v.pos == null) continue;
            visited.add(v);
            if(v.dist >= radius) continue;
            for(int i = 0; i < 6; i++){
                int nx = mvarr3[i] + v.pos.getX();
                int ny = mvarc3[i] + v.pos.getY();
                int nz = mvarz3[i] + v.pos.getZ();
                var bpos = new BlockPos(nx, ny, nz);
                if(!isWalkableThrough(world, bpos) || !world.hasNode(bpos)) continue;
                var cblock = new QueuedBlock(bpos, v.dist + 1);
                if(visited.contains(cblock)) continue;
                visited.add(cblock);
                dq.add(cblock);
            }
        }
        return visited.stream().map(x->world.getNode(x.pos)).toList();
    }

    public static BlockPos getSurfaceLevelBlock(Level world, Vec3i pos){
        for(int i = -2; i <= 2; i++){
            var cpos = new BlockPos(pos.getX(), i + pos.getY(), pos.getZ());
            if(checkWalkableBlock(world, cpos)){
                return cpos;
            }
        }
        return null;
    }
    public static BlockPos getSurfaceLevelBlock(JourneyWorld world, Vec3i pos){
        for(int i = -2; i <= 2; i++){
            var cpos = new BlockPos(pos.getX(), i + pos.getY(), pos.getZ());
            if(checkWalkableBlock(world, cpos)){
                return cpos;
            }
        }
        return null;
    }
    private static boolean isWalkableThrough(Level world, BlockPos pos){
        var state = world.getBlockState(pos);
        if(state.isAir()) return true;
        if(!state.getMaterial().blocksMotion()) return true;
        return false;
    }
    private static boolean isWalkableThrough(JourneyWorld world, Vec3i pos){
        if(!world.hasNode(pos)) return false;
        return world.getNode(pos).isAir;
    }
    private static boolean checkWalkableBlock(Level world, BlockPos pos){
        return !isWalkableThrough(world, pos) && isWalkableThrough(world, pos.above()) && isWalkableThrough(world, pos.above(2));
    }
    private static boolean checkWalkableBlock(JourneyWorld world, Vec3i pos){
        if(!world.hasNode(pos) || !world.hasNode(pos.above()) || !world.hasNode(pos.above(2))) return false;
        return !isWalkableThrough(world, pos) && isWalkableThrough(world, pos.above()) && isWalkableThrough(world, pos.above(2));
    }
}
