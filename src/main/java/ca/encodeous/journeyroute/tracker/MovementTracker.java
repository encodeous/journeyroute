package ca.encodeous.journeyroute.tracker;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.events.RenderEvent;
import ca.encodeous.journeyroute.events.TickEvent;
import ca.encodeous.journeyroute.utils.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.stream.Collectors;

public class MovementTracker {
    private static ArrayDeque<Vec3i> points = new ArrayDeque<>();
    @EventHandler
    public static void tick(TickEvent event){
        var curPos = MinecraftClient.getInstance().player.getPos();
        var world = MinecraftClient.getInstance().world;
        var bpos = new BlockPos(curPos);
        try{
            var neighbours = WorldUtils.getTraversableBlocks(world, bpos, 4);
            var date = new Date();
            for(var v : neighbours){
                var node = JourneyRoute.INSTANCE.World.getNode(v.pos);
                node.lastVisit = date.getTime();
                node.weighting = Math.min(node.weighting, v.dist / 4.0);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        if(MinecraftClient.getInstance().player.getMainHandStack().getItem().isFood()){
            JourneyRoute.INSTANCE.World.nodeMap.clear();
        }
//        if(!points.isEmpty()){
//            var prevPos = points.peekLast();
//            if(prevPos.getSquaredDistance(curPos) < 1){
//                return;
//            }
//        }
//        points.add(bpos);
//        if(points.size() > 1000){
//            points.poll();
//        }
    }
    @EventHandler
    private static void render(RenderEvent event){
        var renderer = event.getRenderer();
        try{
            for(var x : JourneyRoute.INSTANCE.World.nodeMap.entrySet()){
                var pt = x.getKey();
                renderer.drawShapeOutline(VoxelShapes.fullCube(), new Vec3d(pt.getX(), pt.getY(), pt.getZ()), new Color(100, 100, 250, 255 - (int)(x.getValue().weighting * 254)), 1.01f);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
//        if(points.size() >= 2){
//            var tmp =
//                    points.stream().map((citr)->new Vec3d(citr.getX() + 0.5, citr.getY() + 0.5, citr.getZ() + 0.5)).collect(Collectors.toCollection(ArrayDeque::new));
////            tmp = rdpSimplification(1, tmp);
////            for(int i = 0; i < 4; i++){
////                tmp = chaikinIter(0.25, tmp);
////            }
//            var itr = tmp.iterator();
//            Vec3d prev = null;
//            while(itr.hasNext()){
//                if(prev == null){
//                    prev = itr.next();
//                }
//                else{
//                    var citr = itr.next();
//                    Vec3d cur = citr;
//                    event.getRenderer().drawLine(prev, cur, Color.CYAN);
//                    prev = cur;
//                }
//            }
//        }
    }

    public static double getDist(Vec3d ep1, Vec3d ep2, Vec3d pt){
        // https://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
        var d1 = pt.subtract(ep1);
        var d2 = pt.subtract(ep2);
        var d3 = ep2.subtract(ep1);
        return (d1.crossProduct(d2).length()) / d3.length();
    }

    public static ArrayDeque<Vec3d> rdpSimplification(double epsilon, ArrayDeque<Vec3d> input){
        if(input.size() <= 1) return input;
        var p1 = input.getFirst();
        var p2 = input.getLast();
        var arr1 = new ArrayDeque<Vec3d>();
        var arr2 = new ArrayDeque<Vec3d>();
        int idx = 0;
        double md = 0;
        int mdi = 0;
        for(var point : input){
            if(idx != 0 && idx != input.size() - 1){
                var d = getDist(p1, p2, point);
                if(d > md){
                    md = d;
                    mdi = idx;
                }
            }
            idx++;
        }
        if(md < epsilon || input.size() == 2){
            var deq = new ArrayDeque<Vec3d>();
            deq.add(p1);
            deq.add(p2);
            return deq;
        }
        idx = 0;
        for(var point : input){
            if(idx < mdi){
                arr1.add(point);
            }
            else if(idx == mdi){
                arr1.add(point);
                arr2.add(point);
            }
            else{
                arr2.add(point);
            }
            idx++;
        }
        var ans = new ArrayDeque<Vec3d>();
        var a1 = rdpSimplification(epsilon, arr1);
        a1.removeLast();
        var a2 = rdpSimplification(epsilon, arr2);
        for(var k : a1){
            ans.add(k);
        }
        for(var k : a2){
            ans.add(k);
        }
        return ans;
    }

    public static ArrayDeque<Vec3d> chaikinIter(double subdivideAmount, ArrayDeque<Vec3d> input){
        var output = new ArrayDeque<Vec3d>();
        var itr = input.iterator();
        Vec3d prev = null;
        output.add(input.getFirst());
        while(itr.hasNext()){
            if(prev == null){
                prev = itr.next();
            }
            else{
                var cur = itr.next();
                double dist = cur.distanceTo(prev);
                var dir = cur.subtract(prev).normalize();
                output.add(prev.add(dir.multiply(dist * subdivideAmount)));
                output.add(cur.add(dir.multiply(dist * -subdivideAmount)));
                prev = cur;
            }
        }
        output.add(input.getLast());
        return output;
    }
}
