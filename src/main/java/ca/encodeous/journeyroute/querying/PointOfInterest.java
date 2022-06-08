package ca.encodeous.journeyroute.querying;

import journeymap.client.api.display.Waypoint;
import net.minecraft.core.Vec3i;

public class PointOfInterest {
    public PointOfInterest(Waypoint waypoint, Vec3i pos) {
        this.waypoint = waypoint;
        this.pos = pos;
    }

    public Waypoint waypoint;
    public Vec3i pos;
}
