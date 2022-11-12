package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

import java.util.Arrays;

public class LinkedSwitchableWidget extends AbstractWidget {

    private SwitchableWidget selectedWidget;
    private SwitchableWidget[] widgets;
    private byte selected;

    public LinkedSwitchableWidget(int pX, int pY, int pWidth, int pHeight, byte selected, SwitchableWidget... widgets) {
        super(pX, pY, pWidth, pHeight, new TextComponent("linked_switchable_widget"));
        selectedWidget = widgets[selected];
        this.widgets = widgets;
        this.selected = selected;
        Arrays.stream(widgets).forEach(switchableWidget -> {
            if (y == 0)
                y = switchableWidget.y; //Set y position of the SLW to the Y value of the first button in the list, must the button determine the height and not the LSW
            switchableWidget.updatePos(pX, pY);
            switchableWidget.setEnabled(widgets[selected].equals(switchableWidget));
        });
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        for (SwitchableWidget w : widgets) {
            w.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        //DEBUG POSITION fill(pPoseStack, x, y, x + width, y + height, 0xaa00ff00);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        boolean clicked = super.mouseClicked(pMouseX, pMouseY, pButton);
        if (clicked) {
            int index = 0;
            for (SwitchableWidget w : widgets) {
                if (w.mouseClicked(pMouseX, pMouseY, pButton)) {
                    selectedWidget = widgets[index];
                    selectedWidget.setEnabled(true);
                }
                index++;
            }
            for (SwitchableWidget w : widgets)
                if (!w.equals(selectedWidget))
                    w.setEnabled(false);
        }
        return clicked;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {

    }
}
