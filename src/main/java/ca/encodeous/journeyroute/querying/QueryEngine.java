package ca.encodeous.journeyroute.querying;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.client.plugin.JourneyMapPlugin;
import ca.encodeous.journeyroute.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.regex.Pattern;

public class QueryEngine {
    private static final Pattern coordMatch = Pattern.compile("[xX\\s,.=:]*(-?\\d+)[yYzZ\\s,.=:]+(-?\\d+)[zZ\\s,.=:]*(-?\\d+)?");
    private static ArrayList<PointOfInterest> waypoints = new ArrayList<>();
    public static void updateWaypoints(){
        waypoints.clear();
        for(var wp : JourneyMapPlugin.CLIENT.getAllWaypoints()){
            var mapped = WorldUtils.getNearestMappedBlockVertical(JourneyRoute.INSTANCE.world, wp.getPosition());
            if(mapped != null){
                waypoints.add(new PointOfInterest(wp, mapped));
            }
        }
    }
    public static List<QueryResult> getResultsForQuery(String query){
        ArrayList<QueryResult> results = new ArrayList<>();
        var matcher = coordMatch.matcher(query);
        if(matcher.matches()){
            try{
                if(matcher.group(3) == null){
                    int x = Integer.parseInt(matcher.group(1));
                    int z = Integer.parseInt(matcher.group(2));
                    ArrayList<Pair<QueryResult, Double>> poiList = new ArrayList<>();
                    for(var poi : waypoints){
                        int dx = x - poi.pos.getX();
                        int dz = z - poi.pos.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if(dist < 50){
                            poiList.add(new Pair<>(new QueryResult(poi), dist));
                        }
                    }
                    poiList.sort(Comparator.comparingDouble(Pair::getB));
                    results.addAll(poiList.stream().map(Pair::getA).toList());
                }else{
                    int x = Integer.parseInt(matcher.group(1));
                    int y = Integer.parseInt(matcher.group(2));
                    int z = Integer.parseInt(matcher.group(3));
                    ArrayList<Pair<QueryResult, Double>> poiList = new ArrayList<>();
                    for(var poi : waypoints){
                        int dx = x - poi.pos.getX();
                        int dy = y - poi.pos.getY();
                        int dz = z - poi.pos.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz + dy * dy);
                        if(dist < 100){
                            poiList.add(new Pair<>(new QueryResult(poi), dist));
                        }
                    }
                    if(y > Minecraft.getInstance().level.getMinBuildHeight() && y < Minecraft.getInstance().level.getMaxBuildHeight()){
                        results.add(new QueryResult(new BlockPos(x, y, z)));
                    }
                    poiList.sort(Comparator.comparingDouble(Pair::getB));
                    results.addAll(poiList.stream().map(Pair::getA).toList());
                }
            }
            catch(NumberFormatException e){

            }
        }
        for (var poi : waypoints) {
            if(poi.waypoint.getName().toLowerCase().contains(query.toLowerCase())){
                results.add(new QueryResult(poi));
            }
        }
        if(results.isEmpty()){
            results.add(new QueryResult(QueryResult.ResultType.NO_RESULTS));
        }
        return results;
    }
}
