package ca.encodeous.journeyroute.querying;

import journeymap.client.api.display.Waypoint;
import net.minecraft.core.Vec3i;

public class QueryResult {
    public String name;

    public Vec3i position;
    public boolean isMapped;

    public QueryResult(String name, Vec3i position, boolean isMapped, Waypoint assocWaypoint) {
        this.name = name;
        this.position = position;
        this.isMapped = isMapped;
        this.assocWaypoint = assocWaypoint;
    }

    public Waypoint assocWaypoint;
}
