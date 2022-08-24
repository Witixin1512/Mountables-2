package witixin.mountables2.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class SwitchableWidget extends AbstractWidget {

    private final ResourceLocation off;
    private final ResourceLocation on;
    private final String name;
    private final String renderText;
    private boolean isEnabled = false;

    public boolean isEnabled(){
        return isEnabled;
    }
    public void setEnabled(boolean bool){
        this.isEnabled = bool;
    }

    public SwitchableWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation off,ResourceLocation on, String toRender, String realName) {
        super(pX, pY, pWidth, pHeight, new TextComponent("switchable_widget"));
        this.off = off;
        this.on = on;
        this.name = realName;
        this.renderText = toRender;
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
        String fixedString = fixName(renderText);
        float amount = Minecraft.getInstance().font.width(fixedString);
        Minecraft.getInstance().font.drawShadow(pPoseStack, fixedString, this.x + 50 - amount / 2f, this.y+6, 0xffffff);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
    private String fixName(String original){
        if (original.contains("_")){
            String[] split = name.split("_");
            return split[0].charAt(0) + split[0].substring(1).toLowerCase() + " " + split[1].substring(0, 1).toUpperCase() + split[1].substring(1).toLowerCase();
        }
        else {
            return original.charAt(0) + original.substring(1).toLowerCase();
        }
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "SwitchableWidget[" + getName() + ":" + isEnabled() + "]";
    }
}
