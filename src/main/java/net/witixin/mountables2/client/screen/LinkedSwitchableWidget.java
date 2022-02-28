package net.witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.witixin.mountables2.network.PacketHandler;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LinkedSwitchableWidget extends AbstractWidget {

    private List<SwitchableWidget> list;
    private SwitchableWidget selectedWidget;
    private final UUID mountID;
    private final Class packetClass;
    private final String TYPE;

    public LinkedSwitchableWidget(int pX, int pY, int pWidth, int pHeight, UUID mount, String toCompare, Class packet, String TYPE,  SwitchableWidget... widgets) {
        super(pX, pY, pWidth, pHeight, new TextComponent("linked_switchable_widget"));
        this.list = Arrays.asList(widgets);
        this.mountID = mount;
        this.selectedWidget = this.list.stream().filter(widget -> widget.getName().matches(toCompare)).findAny().get();
        this.packetClass = packet;
        this.TYPE = TYPE;
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
            sendPacket(mountID, selectedWidget.getName());
            if (Minecraft.getInstance().screen instanceof MountableAIScreen screen){
                screen.setType(TYPE, selectedWidget.getName());
                return true;
            }
            if (Minecraft.getInstance().screen instanceof CommandChipScreen screen){
                screen.setFollowMode(selectedWidget.getName());
            }
            return true;
        }
        return false;
    }
    public void sendPacket(UUID id, String toSend){
        try {
            Object object = packetClass.getConstructor(UUID.class, String.class, String.class).newInstance(id, toSend, TYPE);
            PacketHandler.INSTANCE.sendToServer(object);
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
