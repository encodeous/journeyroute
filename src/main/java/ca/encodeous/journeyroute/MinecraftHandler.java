package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.events.RenderEvent;
import ca.encodeous.journeyroute.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Date;

/**
 * General handler for minecraft events
 */
public class MinecraftHandler {
    /**
     * Handles tick events of the main client thread
     */
    public static void tick(){
        var curPos = Minecraft.getInstance().player.position();
        var world = Minecraft.getInstance().level;
        var bpos = new BlockPos(curPos);
        // auto map neighbour blocks next to the player. This allows journeyroute to discover new routes as the player explores.
        try{
            var neighbours = WorldUtils.getTraversableBlocks(world, bpos, 4);
            var date = new Date();
            for(var v : neighbours){
                var node = JourneyRoute.INSTANCE.world.getNode(v.pos);
                node.lastVisit = date.getTime();
                node.weighting = Math.min(node.weighting, v.dist / 4.0);
                node.isAir = false;
            }
            var air = WorldUtils.getSurroundingAir(world, bpos, 4);
            for(var v : air){
                var node = JourneyRoute.INSTANCE.world.getNode(v.pos);
                node.lastVisit = date.getTime();
                node.weighting = Math.min(node.weighting, v.dist / 4.0);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Handles a single render call, from Minecraft's main render thread
     * @param event contains the required data used by this function
     */
    public static void render(RenderEvent event){
        var renderer = event.getRenderer();
        try{
            // render the route if it exists
            if(JourneyRoute.route != null){
                var camPos = Minecraft.getInstance().cameraEntity.position();
                var itr = JourneyRoute.route.BakedRenderPath.iterator();
                Vec3 prev = null;
                var lnColor = Color.WHITE;
                var lnColor2 = new Color(9, 173, 199);
                double dist = 0;
                boolean draw = true;
                int segCount = 0;
                // shows the player the right way to travel
                boolean hasReachedPlayer = true;
                while(itr.hasNext()){
                    if(prev == null){
                        prev = itr.next();
                    }
                    else{
                        var citr = itr.next();
                        Vec3 cur = citr;
                        dist += cur.distanceTo(prev);
                        if(cur.distanceTo(camPos) <= 3){
                            hasReachedPlayer = false;
                        }
                        // responsible for the dashed lines effect
                        if(dist >= 0.2){
                            dist = 0;
                            draw = !draw;
                        }
                        if(draw){
                            segCount++;
                            // render lines only within render distance
                            if(cur.distanceTo(camPos) <= Minecraft.getInstance().levelRenderer.getLastViewDistance() * 16){
                                renderer.drawLine(prev, cur, (segCount % 2 == 0 || hasReachedPlayer) ? lnColor : lnColor2);
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
