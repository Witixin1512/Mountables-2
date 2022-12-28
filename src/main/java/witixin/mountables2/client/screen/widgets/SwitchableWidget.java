package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;

public class SwitchableWidget extends AbstractWidget {

    private final String renderText;
    private boolean isEnabled = false;
    private static final ResourceLocation BIG_BUTTON_OFF = Mountables2Mod.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Mountables2Mod.rl("gui/mountable_command_button_selected.png");
    private final OnPress onPress;

    public SwitchableWidget(int pX, int pY, int pWidth, int pHeight, String toRender, OnPress onPress) {
        super(pX, pY, pWidth, pHeight, Component.literal("switchable_widget"));
        this.renderText = toRender;
        this.onPress = onPress;
    }

    public void updatePos(int pX, int pY) {
        this.setX(this.getX() + pX);
        this.setY(this.getY() + pY);
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.isEnabled() ? BIG_BUTTON_ON : BIG_BUTTON_OFF);
        blit(pPoseStack, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        float amount = Minecraft.getInstance().font.width(renderText);
        Minecraft.getInstance().font.drawShadow(pPoseStack, renderText, this.getX() + width/2f - amount / 2f, this.getY() + 6, 0xffffff);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean bool) {
        this.isEnabled = bool;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
    @Override
    public String toString() {
        return "SwitchableWidget[" + renderText + ":" + isEnabled() + "]";
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        onPress.onPress(this);
    }

    public interface OnPress {
        void onPress(SwitchableWidget pButton);
    }
}
