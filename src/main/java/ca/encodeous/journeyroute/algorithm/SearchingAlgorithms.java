package ca.encodeous.journeyroute.algorithm;

import ca.encodeous.journeyroute.Config;
import ca.encodeous.journeyroute.utils.WorldUtils;
import ca.encodeous.journeyroute.world.JourneyWorld;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.RouteNode;
import ca.encodeous.journeyroute.world.WorldNode;
import net.minecraft.core.Vec3i;

import java.util.HashMap;
import java.util.PriorityQueue;

public class SearchingAlgorithms {
    /**
     * This method uses the A* heuristic path-finding algorithm to find a semi-optimal path to the destination.
     * The heuristic used is euclidean distance, and weights are calculated based on travel frequency, air-block, and other factors.
     * @param world the world
     * @param cur the current location
     * @param dest the destination location
     * @return an semi-optimal route that connects cur and dest. If not found, null or an empty path will be returned
     */
    public static Route getRouteTo(JourneyWorld world, Vec3i cur, Vec3i dest){
        if(!world.hasNode(cur) || !world.hasNode(dest)) return null;
        var startNode = world.getNode(cur);
        var dist = new HashMap<Vec3i, Double>();
        var prev = new HashMap<Vec3i, Vec3i>();
        var nodes = new PriorityQueue<RouteNode>();
        var srn = new RouteNode(0, 0, startNode);
        nodes.add(srn);
        var route = new Route();
        // add the current source node to the pq
        dist.put(srn.pos, 0d);
        while(!nodes.isEmpty()){
            var v = nodes.poll();
            if(v.pos.equals(dest)) break;
//            route.Path.add(v.pos);
            double w = v.weight;
            if(dist.getOrDefault(v.pos, 1e17) < w) continue;
            // searches & prioritizes ground blocks
            for(var block : WorldUtils.getTraversableBlocks(world, v.pos, 1)){
                double heurWeight = cur.distSqr(dest);
                var nPos = new Vec3i(block.worldX, block.worldY, block.worldZ);
                double newWeight = w + Math.sqrt(v.pos.distSqr(nPos)) + block.weighting;
                if(newWeight < dist.getOrDefault(nPos, 1e17)){
                    dist.put(nPos, newWeight);
                    prev.put(nPos, v.pos);
                    nodes.add(new RouteNode(heurWeight + newWeight, newWeight, block));
                }
            }
            // searches through air blocks that the player can jump down
            for(var block : WorldUtils.getSurroundingAir(world, v.pos, true)){
                double heurWeight = cur.distSqr(dest);
                var nPos = new Vec3i(block.worldX, block.worldY, block.worldZ);
                addAirNodeIfOptimal(dist, prev, nodes, v, w, block, heurWeight, nPos);
            }
            // searches through air blocks where the player can fly up (this is de-prioritized since players cannot fly in survival mode)
            for(var block : WorldUtils.getSurroundingAir(world, v.pos, false)){
                double heurWeight = cur.distSqr(dest) + Config.AIR_WEIGHT;
                var nPos = new Vec3i(block.worldX, block.worldY, block.worldZ);
                addAirNodeIfOptimal(dist, prev, nodes, v, w, block, heurWeight, nPos);
            }
        }
        Vec3i prevNode = dest;
        while(prevNode != null){
            route.Path.addFirst(prevNode);
            prevNode = prev.getOrDefault(prevNode, null);
        }
        return route;
    }

    /**
     * Compares weighting of air nodes and adds the node to the queue if it is optimal
     */
    private static void addAirNodeIfOptimal(HashMap<Vec3i, Double> dist, HashMap<Vec3i, Vec3i> prev, PriorityQueue<RouteNode> nodes, RouteNode v, double w, WorldNode block, double heurWeight, Vec3i nPos) {
        double newWeight = w + Math.sqrt(v.pos.distSqr(nPos)) + block.weighting + Config.AIR_WEIGHT;
        if(newWeight < dist.getOrDefault(nPos, 1e17)){
            dist.put(nPos, newWeight);
            prev.put(nPos, v.pos);
            nodes.add(new RouteNode(heurWeight + newWeight, newWeight, block));
        }
    }
}
