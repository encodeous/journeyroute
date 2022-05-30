package ca.encodeous.journeyroute.tracker;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.events.RenderEvent;
import ca.encodeous.journeyroute.events.TickEvent;
import ca.encodeous.journeyroute.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.stream.Collectors;

public class MovementTracker {
    private static ArrayDeque<Vec3i> points = new ArrayDeque<>();
    public static void tick(TickEvent event){
        var curPos = Minecraft.getInstance().player.position();
        var world = Minecraft.getInstance().level;
        var bpos = new BlockPos(curPos);
        try{
            var neighbours = WorldUtils.getTraversableBlocks(world, bpos, 4);
            var date = new Date();
            for(var v : neighbours){
                var node = JourneyRoute.INSTANCE.World.getNode(v.pos);
                node.lastVisit = date.getTime();
                node.weighting = Math.min(node.weighting, v.dist / 4.0);
                node.isAir = false;
            }
            var air = WorldUtils.getSurroundingAir(world, bpos, 4);
            for(var v : air){
                var node = JourneyRoute.INSTANCE.World.getNode(v.pos);
                node.lastVisit = date.getTime();
                node.weighting = Math.min(node.weighting, v.dist / 4.0);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
//        if(Minecraft.getInstance().player.getMainHandItem().getItem().isEdible()){
//            JourneyRoute.INSTANCE.World.ChunkMap.clear();
//        }
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
    public static void render(RenderEvent event){
        var renderer = event.getRenderer();
        try{
            if(JourneyRoute.Route != null){
                var camPos = Minecraft.getInstance().cameraEntity.position();
                for(var x : JourneyRoute.Route.Path){
                    var pt = x;
                    if(pt.closerToCenterThan(camPos, Minecraft.getInstance().levelRenderer.getLastViewDistance() * 16)){
                        renderer.drawShapeOutline(Shapes.block(), new Vec3(pt.getX(), pt.getY(), pt.getZ()), new Color(100, 100, 250, 180), 1.01f);
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
//        if(points.size() >= 2){
//            var tmp =
//                    points.stream().map((citr)->new Vec3(citr.getX() + 0.5, citr.getY() + 0.5, citr.getZ() + 0.5)).collect(Collectors.toCollection(ArrayDeque::new));
////            tmp = rdpSimplification(1, tmp);
////            for(int i = 0; i < 4; i++){
////                tmp = chaikinIter(0.25, tmp);
////            }
//            var itr = tmp.iterator();
//            Vec3 prev = null;
//            while(itr.hasNext()){
//                if(prev == null){
//                    prev = itr.next();
//                }
//                else{
//                    var citr = itr.next();
//                    Vec3 cur = citr;
//                    event.getRenderer().drawLine(prev, cur, Color.CYAN);
//                    prev = cur;
//                }
//            }
//        }
    }

    public static double getDist(Vec3 ep1, Vec3 ep2, Vec3 pt){
        // https://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
        var d1 = pt.subtract(ep1);
        var d2 = pt.subtract(ep2);
        var d3 = ep2.subtract(ep1);
        return (d1.cross(d2).length()) / d3.length();
    }

    public static ArrayDeque<Vec3> rdpSimplification(double epsilon, ArrayDeque<Vec3> input){
        if(input.size() <= 1) return input;
        var p1 = input.getFirst();
        var p2 = input.getLast();
        var arr1 = new ArrayDeque<Vec3>();
        var arr2 = new ArrayDeque<Vec3>();
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
            var deq = new ArrayDeque<Vec3>();
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
        var ans = new ArrayDeque<Vec3>();
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

    public static ArrayDeque<Vec3> chaikinIter(double subdivideAmount, ArrayDeque<Vec3> input){
        var output = new ArrayDeque<Vec3>();
        var itr = input.iterator();
        Vec3 prev = null;
        output.add(input.getFirst());
        while(itr.hasNext()){
            if(prev == null){
                prev = itr.next();
            }
            else{
                var cur = itr.next();
                double dist = cur.distanceTo(prev);
                var dir = cur.subtract(prev).normalize();
                output.add(prev.add(dir.scale(dist * subdivideAmount)));
                output.add(cur.add(dir.scale(dist * -subdivideAmount)));
                prev = cur;
            }
        }
        output.add(input.getLast());
        return output;
    }
}
