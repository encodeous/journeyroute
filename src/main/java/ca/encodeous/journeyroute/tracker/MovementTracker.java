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
                var itr = JourneyRoute.Route.BakedRenderPath.iterator();
                Vec3 prev = null;
                var lnColor = Color.YELLOW;
                var lnColor2 = Color.BLACK;
                double dist = 0;
                boolean draw = true;
                int segCount = 0;
                while(itr.hasNext()){
                    if(prev == null){
                        prev = itr.next();
                    }
                    else{
                        var citr = itr.next();
                        Vec3 cur = citr;
                        dist += cur.distanceTo(prev);
                        if(dist >= 0.2){
                            dist = 0;
                            draw = !draw;
                        }
                        if(draw){
                            segCount++;
                            if(cur.distanceTo(camPos) <= Minecraft.getInstance().levelRenderer.getLastViewDistance() * 16){
                                renderer.drawLine(prev, cur, segCount % 2 == 0 ? lnColor : lnColor2);
                            }
                        }
                        prev = cur;
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
