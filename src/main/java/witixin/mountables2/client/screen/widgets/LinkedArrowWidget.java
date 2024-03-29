package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class LinkedArrowWidget extends AbstractWidget {
    private final ArrowSelectionWidget left;
    private final ArrowSelectionWidget right;
    private final String buttonName;

    public LinkedArrowWidget(int pX, int pY, int pWidth, int pHeight, ArrowSelectionWidget left, ArrowSelectionWidget right, String buttonName) {
        super(pX, pY, pWidth, pHeight, Component.literal("linked_arrows"));
        this.left = left;
        this.right = right;
        left.updatePos(pX, pY);
        right.updatePos(pX, pY);
        this.buttonName = buttonName;
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick){
        left.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        right.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        Minecraft.getInstance().font.drawShadow(pPoseStack, buttonName.charAt(0) + buttonName.substring(1).toLowerCase(), this.getX() + 52 - Minecraft.getInstance().font.width(buttonName) / 2f, this.getY() + 5, 0xffffff);

    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return right.mouseClicked(pMouseX, pMouseY, pButton) || left.mouseClicked(pMouseX, pMouseY, pButton);
    }

}
