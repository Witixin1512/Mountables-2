package net.witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;



public class ArrowSelectionWidget extends AbstractWidget {

    private final ResourceLocation toRender;

    public ArrowSelectionWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation image) {
        super(pX, pY, pWidth, pHeight, new TextComponent("arrow_selector"));
        this.toRender = image;
    }

    public void updatePos(int pX, int pY){
        this.x += pX;
        this.y += pY;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, toRender);
        this.blit(pPoseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
    }


    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
