package ca.encodeous.journeyroute.querying;

import ca.encodeous.journeyroute.JourneyRoute;
import ca.encodeous.journeyroute.utils.TextUtils;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.awt.*;

public class QueryResultWidget extends WPlainPanel {

    public QueryResult result;

    private BackgroundPainter painter;

    public void configureWidget(QueryResult result, boolean isEven){
        this.result = result;
        var pos = result.position;
        if(result.type.equals(QueryResult.ResultType.COORDINATE)){
            this.add(new WText(new TextComponent(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()))
                    , 0, 0, 160, 1);
            this.add(new WText(new TextComponent("Coordinate")
                    .setStyle(Style.EMPTY.withBold(true))
            ), 0, 10, 20 * 15, 1);
        }
        else if(result.type.equals(QueryResult.ResultType.POI)){
            this.add(new WText(new TextComponent(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()))
                    , 0, 0, 160, 1);
            this.add(new WText(new TextComponent("Waypoint: \"" + result.poi.waypoint.getName() + '"')
                    .setStyle(Style.EMPTY.withBold(true))
            ), 0, 10, 20 * 15, 1);
        }else{
            this.add(new WText(new TextComponent("No mapped destinations have been found that match the query.")
                    .setStyle(Style.EMPTY.withBold(true))
            ), 0, 0, 20 * 15, 1);
        }

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
        if(!result.type.equals(QueryResult.ResultType.NO_RESULTS)){
            JourneyRoute.tryRouteTo(result.position, (success) ->{
                if(success){
                    Minecraft.getInstance().player.displayClientMessage(new TextComponent("Routing started: Showing path to " + TextUtils.formatCoordinate(result.position)), false);
                    Minecraft.getInstance().tell(()->{
                        Minecraft.getInstance().screen.onClose();
                    });
                }else{
                    Minecraft.getInstance().player.displayClientMessage(new TextComponent("Cannot find a route to " + TextUtils.formatCoordinate(result.position)), false);
                }
            });
        }
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
