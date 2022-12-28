package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class ArrowSelectionWidget extends AbstractWidget {

    private final ResourceLocation toRender;
    private final Runnable onPress;

    public ArrowSelectionWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation image, Runnable onButtonPress) {
        super(pX, pY, pWidth, pHeight, Component.literal("arrow_selector"));
        this.toRender = image;
        this.onPress = onButtonPress;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {

        onPress.run();
    }

    public void updatePos(int pX, int pY) {
        this.setX(this.getX() + pX);
        this.setY(this.getY() + pY);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, toRender);
        blit(pPoseStack, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }


    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
