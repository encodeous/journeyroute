package ca.encodeous.journeyroute.events;

import ca.encodeous.journeyroute.rendering.Renderer;

public class RenderEvent {
    private Renderer renderer;

    public Renderer getRenderer() {
        return renderer;
    }

    public RenderEvent(Renderer renderer) {
        this.renderer = renderer;
    }
}
