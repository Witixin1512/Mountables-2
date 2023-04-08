package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public class LinkedSwitchableWidget extends AbstractWidget {

    private SwitchableWidget selectedWidget;
    private SwitchableWidget[] widgets;

    public LinkedSwitchableWidget(int pX, int pY, int pWidth, int pHeight, SwitchableWidget... widgets) {
        super(pX, pY, pWidth, pHeight, Component.literal("linked_switchable_widget"));
        this.widgets = widgets;

        for (SwitchableWidget switchableWidget : widgets) {
            switchableWidget.updatePos(pX,  pY);
        }
    }
    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick){
        for (SwitchableWidget w : widgets) {
            w.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        //fill(pPoseStack, getX(), getY(), getX() + width, getY() + height, 0xaa00ff00);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean clicked = Arrays.stream(widgets).map(switchableWidget -> switchableWidget.mouseClicked(pMouseX, pMouseY, pButton)).reduce(Boolean::logicalOr).get();
        if (clicked) {
            boolean clickedWidget = false;
            int index = 0;
            for (SwitchableWidget w : widgets) {
                if (w.mouseClicked(pMouseX, pMouseY, pButton)) {
                    clickedWidget = true;
                    selectedWidget = widgets[index];
                    selectedWidget.setEnabled(true);
                }
                index++;
            }
            for (SwitchableWidget w : widgets)
                if (clickedWidget && !w.equals(selectedWidget))
                    w.setEnabled(false);
        }
        return clicked;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {

    }
}
