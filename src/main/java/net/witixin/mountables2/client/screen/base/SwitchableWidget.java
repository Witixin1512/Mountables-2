package net.witixin.mountables2.client.screen.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SwitchableWidget extends AbstractWidget {

    private final ResourceLocation off;
    private final ResourceLocation on;
    private final String name;

    private boolean isEnabled = false;

    public boolean isEnabled(){
        return isEnabled;
    }
    public void setEnabled(boolean bool){
        this.isEnabled = bool;
    }

    public SwitchableWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation off,ResourceLocation on, String name) {
        super(pX, pY, pWidth, pHeight, new TextComponent("switchable_widget"));
        this.off = off;
        this.on = on;
        this.name = name;
    }

    public void updatePos(int pX, int pY){
        this.x += pX;
        this.y += pY;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.isEnabled() ? on : off);
        this.blit(pPoseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        Minecraft.getInstance().font.drawShadow(pPoseStack,getName().substring(0, 1) + getName().substring(1).toLowerCase(), this.x + 50 - Minecraft.getInstance().font.width(getName()) / 2, this.y+6, 0xffffff);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "SwitchableWidget[" + getName() + ":" + isEnabled() + "]";
    }
}
