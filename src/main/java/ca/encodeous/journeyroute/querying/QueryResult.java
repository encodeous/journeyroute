package ca.encodeous.journeyroute.querying;

import net.minecraft.core.Vec3i;

/**
 * A class that represents data returned by the query engine
 */

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
        /**
         * Represents a point of interest, such as a waypoint
         */
        POI,
        /**
         * Represents a mapped coordinate
         */
        COORDINATE,
        /**
         * Represents that a query has fetched no results. Should be the only result returned
         */
        NO_RESULTS,
        /**
         * DEBUG: Saves the current route into a file
         */
        SAVE_PATH,
        /**
         * DEBUG: Loads the current saved route from disk
         */
        LOAD_PATH
    }
}
