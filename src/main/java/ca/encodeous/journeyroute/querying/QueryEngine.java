package ca.encodeous.journeyroute.querying;

import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryEngine {
    public static List<QueryResult> getResultsForQuery(String query){
        ArrayList<QueryResult> results = new ArrayList<>();
        for(int i = 0; i < query.length(); i++){
            results.add(new QueryResult("Waypoint " + (i + 1), new Vec3i(0, 1, 2), true));
        }
        return results;
    }
}
