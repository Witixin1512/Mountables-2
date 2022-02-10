package net.witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.witixin.mountables2.client.screen.base.SwitchableWidget;
import net.witixin.mountables2.network.PacketHandler;
import net.witixin.mountables2.network.ServerUpdateMountFollowTypePacket;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LinkedSwitchableWidget extends AbstractWidget {

    private List<SwitchableWidget> list;
    private SwitchableWidget selectedWidget;
    private final UUID mountID;

    public LinkedSwitchableWidget(int pX, int pY, int pWidth, int pHeight, UUID mount, String followMode, SwitchableWidget... widgets) {
        super(pX, pY, pWidth, pHeight, new TextComponent("linked_switchable_widget"));
        this.list = Arrays.asList(widgets);
        this.mountID = mount;
        this.selectedWidget = this.list.stream().filter(widget -> widget.getName().matches(followMode)).findAny().get();
        list.forEach(widget -> {
            widget.updatePos(pX, 0);
            if (widget.getName().matches(selectedWidget.getName())) {
                widget.setEnabled(true);
            }
        });
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (SwitchableWidget widget : list) {
            if (widget.mouseClicked(pMouseX, pMouseY, pButton)){
                widget.setEnabled(true);
                selectedWidget = widget;
                break;
            }
        }
        if (selectedWidget.mouseClicked(pMouseX, pMouseY, pButton)){
            for (SwitchableWidget widget : list){
                if (selectedWidget != null && widget != null && selectedWidget != widget){
                    widget.setEnabled(false);
                }
            }
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(mountID, selectedWidget.getName()));
            return true;
        }
        return false;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        for (SwitchableWidget w : list){
            w.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
