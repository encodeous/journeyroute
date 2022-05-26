package ca.encodeous.journeyroute.querying;

import net.minecraft.core.Vec3i;

public class QueryResult {
    public String name;

    public QueryResult(String name, Vec3i position, boolean isMapped) {
        this.name = name;
        this.position = position;
        this.isMapped = isMapped;
    }

    public Vec3i position;
    public boolean isMapped;
}
