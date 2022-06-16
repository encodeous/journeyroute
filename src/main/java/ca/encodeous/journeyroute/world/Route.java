package ca.encodeous.journeyroute.world;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * A class that represents a whole path.
 */
public class Route {
    public ArrayDeque<Vec3i> Path = new ArrayDeque<>();
    public ArrayDeque<Vec3> BakedRenderPath = null;
    private ArrayDeque<Vec3> BakedJourneyMapPolyline = null;
    public ArrayDeque<Vec3> BakedJourneyMapPolygon = null;

    /**
     * Called when a route is fully generated.
     * This ensures the path is ready to be performantly rendered and displayed in-game
     */
    public void bakeRenderPath(){
        // check if the path is actually a valid path
        if(Path.size() >= 2){
            BakedRenderPath = Path.stream().map((citr)->new Vec3(citr.getX() + 0.5, citr.getY() + 2.0, citr.getZ() + 0.5)).collect(Collectors.toCollection(ArrayDeque::new));
            BakedRenderPath = rdpSimplification(1, BakedRenderPath);
            BakedJourneyMapPolyline = new ArrayDeque<>(BakedRenderPath);
            BakedJourneyMapPolyline = rdpSimplification(3, BakedJourneyMapPolyline);
            buildPolygonFromLine(2);
            for(int i = 0; i < 3; i++){
                BakedRenderPath = chaikinIter(0.25, BakedRenderPath);
            }
        }
    }

    /**
     * Builds a polygon from a polyline with a given width
     * Constructs the polygon in CCW
     * @param width the width of the line
     */
    public void buildPolygonFromLine(float width){
        BakedJourneyMapPolygon = new ArrayDeque<>();
        var ditr = BakedJourneyMapPolyline.descendingIterator();
        buildPolyline(width, ditr);
        var itr = BakedJourneyMapPolyline.iterator();
        buildPolyline(width, itr);
    }

    /**
     * Finds a perpendicular line to a 3d line that is on the same x-z plane.
     * @param a the first point
     * @param b the second point
     * @param mag the length of the vector
     * @return a vector that is perpendicular
     */
    private static Vec3 getNormalVectorPlane(Vec3 a, Vec3 b, float mag){
        var vec = a.subtract(b);
        return new Vec3(vec.z, 0, -vec.x).normalize().scale(mag);
    }

    /**
     * Generates a uni-directional polyline that is offset by width that is normal to each individual segment of the line.
     * @param width offset in the direction normal to each line segment
     * @param itr the source of the polyline points
     */
    private void buildPolyline(float width, Iterator<Vec3> itr) {
        Vec3 prev = null;
        while(itr.hasNext()){
            if(prev == null){
                prev = itr.next();
            }
            else{
                var cur = itr.next();
                BakedJourneyMapPolygon.add(prev.add(getNormalVectorPlane(prev, cur, width)));
                BakedJourneyMapPolygon.add(cur.add(getNormalVectorPlane(prev, cur, width)));
                prev = cur;
            }
        }
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
