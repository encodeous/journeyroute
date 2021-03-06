package ca.encodeous.journeyroute.utils;

import ca.encodeous.journeyroute.world.JourneyWorld;
import ca.encodeous.journeyroute.world.RouteNode;
import ca.encodeous.journeyroute.world.WorldNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A collection of utility methods that is used for querying the world
 */

public class WorldUtils {
    /**
     * A class that represents an internal block used by the breadth-first based neighbour block update
     */
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

    /**
     * Finds the nearest block that is mapped that has the same x-z coordinate
     * @param world the world
     * @param pos the block's position
     * @return the nearest mapped block
     */
    public static Vec3i getNearestMappedBlockVertical(JourneyWorld world, Vec3i pos) {
        int y = pos.getY();
        for(int i = 0; i < 319; i++){
            var down = new Vec3i(pos.getX(), y - i, pos.getZ());
            if(world.hasNode(down)) return down;
            var up = new Vec3i(pos.getX(), y - i, pos.getZ());
            if(world.hasNode(up)) return up;
        }
        return null;
    }

    /**
     * Offsets for 2d grid-based breadth-first search
     */
    private static final int[] mvarr = {0, 0, 1, -1}, mvarc = {1, -1, 0, 0};
    /**
     * Offsets for 3d grid-based breadth-first search
     */
    private static final int[]
            mvarr3 = {0, 0, 0, 0, 1, -1},
            mvarc3 = {0, 0, 1, -1, 0, 0},
            mvarz3 = {1, -1, 0, 0, 0, 0};

    /**
     * A method that gets the blocks that can be reached from the player given maximal travel radius
     * @param world the world
     * @param originPos the position of the player
     * @param radius the maximal travel distance
     * @return a set of the blocks that can be reached
     */
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

    /**
     * A method that gets the blocks that can be reached from the player given maximal travel radius
     * @param world the world
     * @param originPos the position of the player
     * @param radius the maximal travel distance
     * @return a set of the blocks that can be reached
     */
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

    /**
     * Gets the reachable air surrounding a player given maximal radius
     * @param world the world
     * @param originPos the player's location
     * @param radius the maximal search radius
     * @return a list of air blocks
     */
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
    /**
     * Gets the reachable air surrounding a player given maximal radius
     * @param world the world
     * @param originPos the player's location
     * @param fallMode if true, the searcher will only search for air blocks below the player
     * @return a list of air blocks
     */
    public static List<WorldNode> getSurroundingAir(JourneyWorld world, Vec3i originPos, boolean fallMode){
        if(originPos == null || !world.hasNode(originPos)) return Collections.emptyList();
        var dq = new ArrayList<WorldNode>();
        for(int i = 0; i < 6; i++){
            int nx = mvarr3[i] + originPos.getX();
            int ny = mvarc3[i] + originPos.getY();
            if(fallMode){
                if(mvarc3[i] != -1) continue;
            }else{
                if(mvarc3[i] == -1) continue;
            }
            int nz = mvarz3[i] + originPos.getZ();
            var bpos = new BlockPos(nx, ny, nz);
            if(!isWalkableThrough(world, bpos) || !world.hasNode(bpos)) continue;
            dq.add(world.getNode(bpos));
        }
        return dq;
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
