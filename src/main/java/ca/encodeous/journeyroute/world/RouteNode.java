package ca.encodeous.journeyroute.world;

import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a block that exists on a path
 */
public class RouteNode implements Comparable<RouteNode> {
    /**
     * The weighting of the block presented to the A* algorithm, not what the actual weight of the blockis
     */
    public double heuristicWeight;
    /**
     * The actual weight of the block
     */
    public double weight;
    /**
     * The underlying world node of the block
     */
    public WorldNode node;
    /**
     * The position of the block
     */
    public Vec3i pos;

    public RouteNode(double heuristicWeight, double weight, WorldNode node) {
        this.heuristicWeight = heuristicWeight;
        this.weight = weight;
        this.node = node;
        pos = new Vec3i(node.worldX, node.worldY, node.worldZ);
    }
    @Override
    public int compareTo(@NotNull RouteNode o) {
        if(this.heuristicWeight != o.heuristicWeight){
            return Double.compare(this.heuristicWeight, o.heuristicWeight);
        }
        if(this.weight != o.weight){
            return Double.compare(this.weight, o.weight);
        }
        return Integer.compare(this.node.hashCode(), o.node.hashCode());
    }
}
