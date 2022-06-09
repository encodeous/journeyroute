package ca.encodeous.journeyroute.world;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Route {
    public ArrayDeque<Vec3i> Path = new ArrayDeque<>();
    public ArrayDeque<Vec3> BakedRenderPath = null;
    public void bakeRenderPath(){
        if(Path.size() >= 2){
            BakedRenderPath = Path.stream().map((citr)->new Vec3(citr.getX() + 0.5, citr.getY() + 2.0, citr.getZ() + 0.5)).collect(Collectors.toCollection(ArrayDeque::new));
            BakedRenderPath = rdpSimplification(1, BakedRenderPath);
            for(int i = 0; i < 3; i++){
                BakedRenderPath = chaikinIter(0.25, BakedRenderPath);
            }
        }
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
