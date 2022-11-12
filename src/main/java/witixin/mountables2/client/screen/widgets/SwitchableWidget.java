package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;

public class SwitchableWidget extends AbstractWidget {

    private final String name;
    private final String renderText;
    private boolean isEnabled = false;
    private static final ResourceLocation BIG_BUTTON_OFF = Mountables2Mod.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Mountables2Mod.rl("gui/mountable_command_button_selected.png");
    private final OnPress onPress;

    public SwitchableWidget(int pX, int pY, int pWidth, int pHeight, String toRender, String realName, OnPress onPress) {
        super(pX, pY, pWidth, pHeight, new TextComponent("switchable_widget"));
        this.name = realName;
        this.renderText = toRender;
        this.onPress = onPress;
    }

    public void updatePos(int pX, int pY) {
        this.x += pX;
        this.y += pY;
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.isEnabled() ? BIG_BUTTON_ON : BIG_BUTTON_OFF);
        blit(pPoseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        String fixedString = fixName(renderText);
        float amount = Minecraft.getInstance().font.width(fixedString);
        Minecraft.getInstance().font.drawShadow(pPoseStack, fixedString, this.x + width/2f - amount / 2f, this.y + 6, 0xffffff);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean bool) {
        this.isEnabled = bool;
    }

    private String fixName(String original) {
        if (original.contains("_")) {
            String[] split = name.split("_");
            return split[0].charAt(0) + split[0].substring(1).toLowerCase() + " " + split[1].substring(0, 1).toUpperCase() + split[1].substring(1).toLowerCase();
        } else {
            return original.charAt(0) + original.substring(1).toLowerCase();
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public String toString() {
        return "SwitchableWidget[" + getName() + ":" + isEnabled() + "]";
    }

    public String getName() {
        return name;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        onPress.onPress(this);
    }

    public interface OnPress {
        void onPress(SwitchableWidget pButton);
    }
}
