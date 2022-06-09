package ca.encodeous.journeyroute.querying;

import journeymap.client.api.display.Waypoint;
import net.minecraft.core.Vec3i;

public class QueryResult {
    public Vec3i position;
    public ResultType type;
    public PointOfInterest poi;

    public QueryResult(Vec3i pos) {
        type = ResultType.COORDINATE;
        position = pos;
    }
    public QueryResult(PointOfInterest poi) {
        position = poi.pos;
        this.poi = poi;
        type = ResultType.POI;
    }

    public QueryResult(ResultType result) {
        type = result;
    }

    public enum ResultType{
        POI,
        COORDINATE,
        NO_RESULTS
    }
}
