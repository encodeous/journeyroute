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
    private WPlainPanel root;
    private final int grid = 15;

    private void generateGui(){
        int width = 22 * grid;
        int height = 16 * grid;
        root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(width, height);
        root.setInsets(Insets.ROOT_PANEL);

        var title = new WText(new TextComponent("JourneyRoute").setStyle(Style.EMPTY.withBold(true)));
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        root.add(title, 0, 0, 20 * grid, grid);

        // searcher

        createSearcher();
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
        resultsList.setListItemHeight(2 * grid);

        searchBar.setChangedListener((s)->{
            resultsList.setData(QueryEngine.getResultsForQuery(s));
        });

        var label = new WText(new TextComponent("Search for Destinations"));

        panel.add(label, 0, 0, 20 * grid, grid);
        panel.add(searchBar, 0, 10, 20 * grid, grid);
        panel.add(resultsList, 0, 2 * grid + 2, 20 * grid, 11 * grid);
        root.add(panel, 6, grid + 5, 20 * grid, 13 * grid);
        searchBar.setHost(this);
        panel.setHost(this);
        searchBar.requestFocus();
        panel.requestFocus();
        return panel;
    }
}
