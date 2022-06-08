package ca.encodeous.journeyroute.client.plugin;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.querying.QueryEngine;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;

import java.util.EnumSet;

public class JourneyMapPlugin implements IClientPlugin {
    public static IClientAPI CLIENT;
    @Override
    public void initialize(IClientAPI jmClientApi) {
        CLIENT = jmClientApi;
        jmClientApi.subscribe(getModId(), EnumSet.of(ClientEvent.Type.MAPPING_STARTED, ClientEvent.Type.WAYPOINT));
    }

    @Override
    public String getModId() {
        return JourneyRoute.MODID;
    }

    @Override
    public void onEvent(ClientEvent event) {
        QueryEngine.updateWaypoints();
    }
}
