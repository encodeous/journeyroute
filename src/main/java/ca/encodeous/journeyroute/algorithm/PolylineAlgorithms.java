package ca.encodeous.journeyroute.algorithm;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;

public class PolylineAlgorithms {
    /**
     * Finds a perpendicular line to a 3d line that is on the same x-z plane.
     * @param a the first point
     * @param b the second point
     * @param mag the length of the vector
     * @return a vector that is perpendicular
     */
    public static Vec3 getNormalVectorPlane(Vec3 a, Vec3 b, float mag){
        var vec = a.subtract(b);
        return new Vec3(vec.z, 0, -vec.x).normalize().scale(mag);
    }

    /**
     * Gets the distance between a 3d line segment and a point
     * @param ep1 the first endpoint
     * @param ep2 the second endpoint
     * @param pt the point
     * @return the minimum distance from the point to the line segment
     */
    public static double getDist(Vec3 ep1, Vec3 ep2, Vec3 pt){
        // https://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
        var d1 = pt.subtract(ep1);
        var d2 = pt.subtract(ep2);
        var d3 = ep2.subtract(ep1);
        return (d1.cross(d2).length()) / d3.length();
    }

    /**
     * Implements the Ramer-Douglas-Peucker polyline simplification algorithm
     * @param epsilon the distance within which a bend is considered a straight line
     * @param input a polyline
     * @return the simplified polyline
     */
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
        ans.addAll(a1);
        ans.addAll(a2);
        return ans;
    }

    /**
     * An implementation of the chaikin polyline smoothing algorithm for a single iteration
     * @param subdivideAmount the distance from each point on the polyline to subdivide
     * @param input a polyline
     * @return the smoothened polyline
     */
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
