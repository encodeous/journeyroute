package ca.encodeous.journeyroute.client.plugin;

import ca.encodeous.journeyroute.JourneyRoute;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;

public class JourneyMapPlugin implements IClientPlugin {
    @Override
    public void initialize(IClientAPI jmClientApi) {
        System.out.println("Initialized");
    }

    @Override
    public String getModId() {
        return JourneyRoute.MODID;
    }

    @Override
    public void onEvent(ClientEvent event) {

    }
}
