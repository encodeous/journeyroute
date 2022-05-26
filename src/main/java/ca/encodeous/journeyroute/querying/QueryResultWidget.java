package ca.encodeous.journeyroute.querying;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.network.chat.TextComponent;

import java.awt.*;

public class QueryResultWidget extends WPlainPanel {

    public QueryResult result;

    private BackgroundPainter painter;

    public void configureWidget(QueryResult result, boolean isEven){
        this.result = result;
        this.add(new WText(new TextComponent(result.name)), 0, 0, 160, 1);
        if(isEven) {
            painter = (matrices, left, top, panel) -> {
                ScreenDrawing.coloredRect(matrices, left, top, panel.getWidth(), panel.getHeight(), Color.lightGray.getRGB());
            };
        }else{
            painter = (matrices, left, top, panel) -> {
                ScreenDrawing.coloredRect(matrices, left, top, panel.getWidth(), panel.getHeight(), new Color(230, 230, 230).getRGB());
            };
        }
        this.setBackgroundPainter(painter);
        this.setInsets(Insets.ROOT_PANEL);
//        var clip = new Clipboard();
//        clip.setClipboard(MinecraftClient.getInstance().getWindow().getHandle(), result.name);
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        this.setBackgroundPainter((matrices, left, top, panel)->{
            ScreenDrawing.coloredRect(matrices, left, top, panel.getWidth(), panel.getHeight(), Color.gray.getRGB());
        });
        return InputResult.IGNORED;
    }

    @Override
    public InputResult onMouseUp(int x, int y, int button) {
        this.setBackgroundPainter(painter);
        return InputResult.IGNORED;
    }
}
