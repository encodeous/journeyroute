package ca.encodeous.journeyroute.gui;

import ca.encodeous.journeyroute.querying.QueryResult;
import ca.encodeous.journeyroute.querying.QueryResultWidget;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WListPanel;

import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SearchPanel extends WListPanel<QueryResult, QueryResultWidget> {
    /**
     * Constructs a list panel.
     *
     * @param data         the list data
     * @param supplier     the widget supplier that creates unconfigured widgets
     * @param configurator the widget configurator that configures widgets to display the passed data
     */
    public SearchPanel(List<QueryResult> data, Supplier<QueryResultWidget> supplier, BiConsumer<QueryResult, QueryResultWidget> configurator) {
        super(data, supplier, configurator);
        this.setBackgroundPainter((matrices, left, top, panel)->{
            ScreenDrawing.coloredRect(matrices, left, top, panel.getWidth(), panel.getHeight(), Color.DARK_GRAY.getRGB());
        });
    }

    public void setData(List<QueryResult> data) {
        this.data = data;
        scrollBar.setMaxValue(data.size());
        scrollBar.setParent(this);
        layout();
    }
}
