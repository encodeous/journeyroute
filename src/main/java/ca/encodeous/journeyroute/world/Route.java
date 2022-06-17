package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Collectors;

import static ca.encodeous.journeyroute.algorithm.PolylineAlgorithms.*;

/**
 * A class that represents a whole path.
 */
public class Route implements DataStorable {
    public ArrayDeque<Vec3i> Path = new ArrayDeque<>();
    public ArrayDeque<Vec3> BakedRenderPath = null;
    private ArrayDeque<Vec3> BakedJourneyMapPolyline = null;
    public ArrayDeque<Vec3> BakedJourneyMapPolygon = null;
    public boolean wasLoadedFromFile = false;

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

    private Vec3 readVec3(ByteBuf in){
        return new Vec3(in.readDouble(), in.readDouble(), in.readDouble());
    }

    public void writeVec3(ByteBuf out, Vec3 data){
        out.writeDouble(data.x);
        out.writeDouble(data.y);
        out.writeDouble(data.z);
    }

    @Override
    public void write(ByteBuf out) {
        out.writeInt(BakedRenderPath.size());
        for(var v : BakedRenderPath){
            writeVec3(out, v);
        }

        out.writeInt(BakedJourneyMapPolygon.size());
        for(var v : BakedJourneyMapPolygon){
            writeVec3(out, v);
        }
    }

    @Override
    public void read(ByteBuf in) {
        this.wasLoadedFromFile = true;
        var pathLen = in.readInt();
        BakedRenderPath = new ArrayDeque<>(pathLen);
        for(int i = 0; i < pathLen; i++){
            var k = readVec3(in);
            BakedRenderPath.add(k);
        }
        var polyLen = in.readInt();
        BakedJourneyMapPolygon = new ArrayDeque<>(polyLen);
        for(int i = 0; i < polyLen; i++){
            var k = readVec3(in);
            BakedJourneyMapPolygon.add(k);
        }
    }
}
