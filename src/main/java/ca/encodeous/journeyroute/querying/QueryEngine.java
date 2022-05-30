package ca.encodeous.journeyroute.querying;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.client.plugin.JourneyMapPlugin;
import ca.encodeous.journeyroute.world.JourneyWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class QueryEngine {
    private static final Pattern coordMatch = Pattern.compile("[xX\\s,.=:]*(-?\\d+)[yY\\s,.=:]+(-?\\d+)[zZ\\s,.=:]+(-?\\d+)");
    public static List<QueryResult> getResultsForQuery(String query){
        ArrayList<QueryResult> results = new ArrayList<>();
        var matcher = coordMatch.matcher(query);
        if(matcher.matches()){
            try{
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                if(y > Minecraft.getInstance().level.getMinBuildHeight() && y < Minecraft.getInstance().level.getMaxBuildHeight()){
                    var pos = new BlockPos(x, y, z);
                    results.add(new QueryResult("Coordinate at @ " + pos, pos, JourneyRoute.INSTANCE.World.hasNode(pos), null));
                }
            }catch(NumberFormatException e){

            }
        }
        var waypoints = JourneyMapPlugin.CLIENT.getAllWaypoints();
        for (var waypoint : waypoints) {
            if(waypoint.getName().toLowerCase().contains(query.toLowerCase())){
                results.add(new QueryResult("JM Waypoint @ " + waypoint.getPosition(),  waypoint.getPosition(), true, waypoint));
            }
        }
        return results;
    }
}
