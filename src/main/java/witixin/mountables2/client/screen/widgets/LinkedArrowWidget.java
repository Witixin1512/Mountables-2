package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import witixin.mountables2.network.PacketHandler;

import java.util.UUID;

public class LinkedArrowWidget extends AbstractWidget {

    private final ArrowSelectionWidget left;
    private final UUID mountID;
    private final ArrowSelectionWidget right;
    private final Class packetClass;
    private final String toRender;

    public LinkedArrowWidget(int pX, int pY, int pWidth, int pHeight, UUID mountable, ArrowSelectionWidget left, ArrowSelectionWidget right, Class packetClass, String toRender) {
        super(pX, pY, pWidth, pHeight, new TextComponent("linked_arrows"));
        this.left = left;
        this.right = right;
        this.mountID = mountable;
        this.packetClass = packetClass;
        left.updatePos(pX, pY);
        right.updatePos(pX, pY);
        this.toRender = toRender;
    }



    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        left.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        right.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        Minecraft.getInstance().font.drawShadow(pPoseStack,toRender.charAt(0) + toRender.substring(1).toLowerCase(), this.x + 52 - Minecraft.getInstance().font.width(toRender) / 2f, this.y+5, 0xffffff);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (left.mouseClicked(pMouseX, pMouseY, pButton) || right.mouseClicked(pMouseX, pMouseY, pButton)){
            try {
                int toMove = this.left.mouseClicked(pMouseX, pMouseY, pButton) ? -1 : 1;
                 PacketHandler.INSTANCE.sendToServer(packetClass.getConstructor(UUID.class, Integer.class).newInstance(mountID, toMove));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
