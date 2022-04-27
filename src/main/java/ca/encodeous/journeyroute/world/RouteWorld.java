package ca.encodeous.journeyroute.world;

import net.minecraft.util.math.Vec3i;

import java.util.HashMap;

public class RouteWorld {
    public HashMap<Vec3i, RouteNode> nodeMap = new HashMap<>();
    public RouteNode getNode(Vec3i pos){
        if (!nodeMap.containsKey(pos)) {
            var cnode = new RouteNode();
            cnode.weighting = 10000;
            nodeMap.put(pos, cnode);
        }
        return nodeMap.get(pos);
    }
    public RouteWorld(){

    }
}
