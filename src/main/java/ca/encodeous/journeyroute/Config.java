package ca.encodeous.journeyroute;

/**
 * A class containing parameters that can be adjusted
 */
public class Config {
    /**
     * The path-finding weight of air blocks
     */
    public static final double AIR_WEIGHT = 200;
    /**
     * The proximity distance at which a coordinate query will return a waypoint. Applicable in x-z plane
     */
    public static final int FUZZY_DISTANCE_2D = 50;
    /**
     * The proximity distance at which a coordinate query will return a waypoint. Applicable in 3d
     */
    public static final int FUZZY_DISTANCE_3D = 100;
}
