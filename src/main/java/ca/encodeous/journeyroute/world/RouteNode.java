package ca.encodeous.journeyroute.world;

import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;


public class RouteNode implements Comparable<RouteNode> {
    public double heuristicWeight;
    public double weight;
    public WorldNode node;
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
