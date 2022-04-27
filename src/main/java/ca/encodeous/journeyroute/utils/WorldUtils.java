package ca.encodeous.journeyroute.utils;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;

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
    public static HashSet<QueuedBlock> getTraversableBlocks(ClientWorld world, BlockPos originPos, int radius){
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
    private static BlockPos getSurfaceLevelBlock(ClientWorld world, BlockPos pos){
        for(int i = -2; i <= 1; i++){
            var cpos = new BlockPos(pos.getX(), i + pos.getY(), pos.getZ());
            if(checkWalkableBlock(world, cpos)){
                return cpos;
            }
        }
        return null;
    }
    private static boolean isWalkableThrough(ClientWorld world, BlockPos pos){
        var state = world.getBlockState(pos);
        if(state.isAir()) return true;
        if(!state.getMaterial().blocksMovement()) return true;
        return false;
    }
    private static boolean checkWalkableBlock(ClientWorld world, BlockPos pos){
        return !isWalkableThrough(world, pos) && isWalkableThrough(world, pos.up()) && isWalkableThrough(world, pos.up(2));
    }
}
