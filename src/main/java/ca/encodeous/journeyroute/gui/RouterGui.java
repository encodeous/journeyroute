package ca.encodeous.journeyroute.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.io.PrintStream;

public class RouterGui extends LightweightGuiDescription {
    public RouterGui(){
        generateGui();
    }

    private WGridPanel advancedPanel, simplifiedPanel;
    private boolean isAdvanced = false;
    private WGridPanel root;

    private void generateGui(){
        int width = 18 * 15;
        int height = 13 * 15;
        root = new WGridPanel();
        setRootPanel(root);
        root.setSize(width, height);
        root.setInsets(Insets.ROOT_PANEL);

        var title = new WText(new LiteralText("JourneyRoute - Path Router"));
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        root.add(title, 0, 0, 10, 1);

        advancedPanel = generateAdvancedPanel();
        simplifiedPanel = generateSimplifiedPanel();

        var advanced = new WToggleButton();
        advanced.setLabel(Text.of("Show Advanced Router"));
        advanced.setOnToggle((val)->{
            isAdvanced = val.booleanValue();
            updatePanel();
        });
        root.add(advanced, 10, 0, 6, 1);

        updatePanel();

        // bottom actions

        var generateRoute = new WButton(new TranslatableText("Generate Route"));
        root.add(generateRoute, 7, 12, 6, 1);
        WButton button = new WButton(new TranslatableText("Clear Route"));
        root.add(button, 13, 12, 5, 1);
        root.validate(this);
    }

    private void updatePanel(){
        if(isAdvanced){
            root.remove(simplifiedPanel);
            root.add(advancedPanel, 1, 1);
        }else{
            root.remove(advancedPanel);
            root.add(simplifiedPanel, 1, 1);
        }
    }

    private WGridPanel generateAdvancedPanel(){
        var panel = new WGridPanel();
        panel.setSize(17, 10);
        panel.setInsets(Insets.NONE);
        panel.add(new WText(new LiteralText("Advanced Routing")), 0, 0, 5, 1);
        return panel;
    }

    private WGridPanel generateSimplifiedPanel(){
        var panel = new WGridPanel();
        panel.setSize(17, 10);
        panel.setInsets(Insets.NONE);
        panel.add(new WText(new LiteralText("Quick Routing")), 0, 0, 5, 1);
        return panel;
    }
}
