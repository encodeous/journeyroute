package ca.encodeous.journeyroute.gui;

import ca.encodeous.journeyroute.querying.QueryEngine;
import ca.encodeous.journeyroute.querying.QueryResultWidget;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class RouterGui extends LightweightGuiDescription {
    public RouterGui(){
        generateGui();
    }

    private WPlainPanel advancedPanel, simplifiedPanel;
    private boolean isAdvanced = false;
    private WPlainPanel root;
    private final int grid = 18;

    private void generateGui(){
        int width = 18 * 15;
        int height = 13 * 15;
        root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(width, height);
        root.setInsets(Insets.ROOT_PANEL);

        var title = new WText(new TextComponent("JourneyRoute - Path Router").setStyle(Style.EMPTY.withBold(true)));
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        root.add(title, 0, 0, 10 * grid, grid);

        // searcher

        root.add(createSearcher(), 6, grid + 5, 10 * grid, 10 * grid);

        // route

        root.add(createRoutePlan(), 11 * grid, grid + 5, 8 * grid, 10 * grid);

        // bottom actions

        var generateRoute = new WButton(new TextComponent("Generate Route"));
        root.add(generateRoute, 0, 12 * grid, 6 * grid, grid);
        WButton button = new WButton(new TextComponent("Clear Route"));
        root.add(button, 6 * grid, 12 * grid, 5 * grid, grid);
        root.validate(this);
    }
    private boolean isEven = true;
    private WWidget createSearcher(){
        var panel = new WPlainPanel();
        var searchBar = new WTextField();
        searchBar.setSuggestion(new TextComponent("Enter Query"));
        searchBar.setEditable(true);
        searchBar.setMaxLength(100);

        var resultsList = new SearchPanel(QueryEngine.getResultsForQuery(""), QueryResultWidget::new, (x, y)->{
            y.configureWidget(x, isEven);
            isEven = !isEven;
        });
        resultsList.setListItemHeight(2 * 15);

        searchBar.setChangedListener((s)->{
            resultsList.setData(QueryEngine.getResultsForQuery(s));
        });

        var label = new WText(new TextComponent("Search for Destinations"));

        panel.add(label, 0, 0, 10 * grid, grid);
        panel.add(searchBar, 0, 10, 10 * grid, grid);
        panel.add(resultsList, 0, 2 * grid + 2, 10 * grid, 8 * grid);
        return panel;
    }

    private WWidget createRoutePlan(){
        var panel = new WPlainPanel();
        var routeNodes = new SearchPanel(QueryEngine.getResultsForQuery(""), QueryResultWidget::new, (x, y)->{
            y.configureWidget(x, isEven);
            isEven = !isEven;
        });
        routeNodes.setListItemHeight(15);
        var label = new WText(new TextComponent("Route Plan"));

        var importBtn = new WButton(new TextComponent("Import"));
        var saveBtn = new WButton(new TextComponent("Save"));
        panel.add(importBtn, 0, 10, 3 * grid, grid);
        panel.add(saveBtn, 3 * grid, 10, 3 * grid, grid);

        panel.add(label, 0, 0, 7 * grid, grid);
        panel.add(routeNodes, 0, 2 * grid + 2, 7 * grid + 7, 8 * grid);
        return panel;
    }
}
