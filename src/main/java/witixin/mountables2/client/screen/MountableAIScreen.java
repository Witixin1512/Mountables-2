package witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.widgets.IndividualSwitchableWidget;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.data.MountableInfo;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerRequestMountableInfoPacket;
import witixin.mountables2.network.server.ServerUpdateMountAIPacket;

public class MountableAIScreen extends CommandChipScreen {

    private SwitchableWidget SCREEN_CHANGE_WIDGET;
    private IndividualSwitchableWidget SWIM_WIDGET;
    private IndividualSwitchableWidget FLY_WIDGET;

    private static final ResourceLocation BIG_BUTTON_OFF = Mountables2Mod.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Mountables2Mod.rl("gui/mountable_command_button_selected.png");


    public MountableAIScreen(MountableInfo info) {
        super(info);
    }

    @Override
    protected void init() {

        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        //FREE_MODE
         final SwitchableWidget WALK = new SwitchableWidget(-140, 50, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.walk"), Mountable.NON_RIDER.WALK.name());
         final SwitchableWidget SLOW_WALK = new SwitchableWidget(-140, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.slow_walk"), Mountable.NON_RIDER.SLOW_WALK.name());
         final SwitchableWidget JUMP = new SwitchableWidget(-140, 110, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.jump"), Mountable.NON_RIDER.JUMP.name());

        //GROUND MODE
         final SwitchableWidget NONE = new SwitchableWidget(-40, 50, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.none"), Mountable.GROUND_MOVEMENT.NONE.name());
         final SwitchableWidget GROUND_WALK = new SwitchableWidget(-40, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.walk"), Mountable.GROUND_MOVEMENT.WALK.name());
         final SwitchableWidget GROUND_SLOW_WALK = new SwitchableWidget(-40, 110, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.slow_walk"), Mountable.GROUND_MOVEMENT.SLOW_WALK.name());
         final SwitchableWidget HOP = new SwitchableWidget(-40, 140, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.hop"), Mountable.GROUND_MOVEMENT.HOP.name());
        //WATER MODE
         final SwitchableWidget FLOAT = new SwitchableWidget(60, 50, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.float"), Mountable.WATER_MOVEMENT.FLOAT.name());
         final SwitchableWidget SINK = new SwitchableWidget(60, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.sink"), Mountable.WATER_MOVEMENT.SINK.name());
         final SwitchableWidget WATER_SWIM = new SwitchableWidget(60, 110, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.swim"), Mountable.WATER_MOVEMENT.SWIM.name());
        //AIR_MODE
         final SwitchableWidget AIR_NONE = new SwitchableWidget(160, 50, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.none"), Mountable.FLYING_MOVEMENT.NONE.name());
         final SwitchableWidget FLIGHT = new SwitchableWidget(160, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.fly"), Mountable.FLYING_MOVEMENT.FLY.name());
         final SwitchableWidget AIR_HOP = new SwitchableWidget(160, 110, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.hop"), Mountable.FLYING_MOVEMENT.HOP.name());


        if (this.canSwim){
            this.SWIM_WIDGET = this.addRenderableWidget(new IndividualSwitchableWidget((posX - 128) /2-140, (posY-256)  /2 + 140, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.swim"), true, trackedMountable, waterState));
            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) /2, (posY-256 ) /2, 100, 60, trackedMountable, this.WATER_MODE, ServerUpdateMountAIPacket.class, Mountable.WATER_MOVEMENT.class.getSimpleName(), FLOAT, SINK, WATER_SWIM));
        }
        if (this.canWalk) {
            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) /2, (posY-256)  /2, 100, 60, trackedMountable, this.NON_RIDER_MODE, ServerUpdateMountAIPacket.class, Mountable.NON_RIDER.class.getSimpleName(), WALK, SLOW_WALK, JUMP));
            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) /2, (posY-256 ) /2, 100, 80, trackedMountable, this.GROUND_MODE, ServerUpdateMountAIPacket.class, Mountable.GROUND_MOVEMENT.class.getSimpleName(), NONE, GROUND_SLOW_WALK, GROUND_WALK, HOP));
        }
        if (this.canFly){
            this.FLY_WIDGET = this.addRenderableWidget(new IndividualSwitchableWidget((posX - 128) /2-140, (posY-256)  /2 + 170, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.fly"), false, trackedMountable, flightState));
            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) /2, (posY-256 ) /2, 100, 60, trackedMountable, this.FLYING_MODE, ServerUpdateMountAIPacket.class, Mountable.FLYING_MOVEMENT.class.getSimpleName(), AIR_NONE, FLIGHT, AIR_HOP));
        }
        this.SCREEN_CHANGE_WIDGET = addRenderableWidget(new SwitchableWidget(((posX - 128) / 2 + 10),(posY -256 )/2 + 200 , 100, 20,  BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("entity.mountables2.mountable_entity"), "Mountable"));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (SCREEN_CHANGE_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            PacketHandler.INSTANCE.sendToServer(new ServerRequestMountableInfoPacket(trackedMountable));
            Minecraft.getInstance().setScreen(new CommandChipScreen(mountableInfo));
            return true;
        }
        if (SWIM_WIDGET != null && SWIM_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            SWIM_WIDGET.setEnabled(!SWIM_WIDGET.isEnabled());
            SWIM_WIDGET.sendPacket();
            this.updateFreeButton(SWIM_WIDGET.isWater(), SWIM_WIDGET.isEnabled());
            return true;
        }
        if (FLY_WIDGET != null && FLY_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            FLY_WIDGET.setEnabled(!FLY_WIDGET.isEnabled());
            FLY_WIDGET.sendPacket();
            this.updateFreeButton(FLY_WIDGET.isWater(), FLY_WIDGET.isEnabled());
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public void updateFreeButton(boolean isWater, boolean toChange){
        if (isWater){
            this.waterState = toChange;
        }
        else {
            this.flightState = toChange;
        }
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        if (canSwim || canFly || canWalk){
            String aiText = I18n.get( "gui.mountables2.ai.ai");
            Minecraft.getInstance().font.drawShadow(pPoseStack, aiText, (posX -128)/2 - 90 - Minecraft.getInstance().font.width(aiText) / 2, (posY -256 )/2 + 30, 0xffffff);
        }
        if (canSwim){
            String swimText = I18n.get("gui.mountables2.ai.swim");
            Minecraft.getInstance().font.drawShadow(pPoseStack, swimText, (posX -128)/2 + 110 - Minecraft.getInstance().font.width(swimText) / 2, (posY -256 )/2 + 30, 0xffffff);
        }
        if (canWalk){
            String walkText = I18n.get("gui.mountables2.ai.walk");
            Minecraft.getInstance().font.drawShadow(pPoseStack, walkText, (posX -128)/2 + 10 - Minecraft.getInstance().font.width(walkText) / 2, (posY -256 )/2 + 30, 0xffffff);
        }
        if (canFly){
            String flyText = I18n.get("gui.mountables2.ai.fly");
            Minecraft.getInstance().font.drawShadow(pPoseStack, flyText, (posX -128)/2 + 210 - Minecraft.getInstance().font.width(flyText) / 2, (posY -256 )/2 + 30, 0xffffff);
        }
    }

    public void setType(String type, String value){
        if (type.matches(Mountable.NON_RIDER.class.getSimpleName())){
            this.NON_RIDER_MODE = value;
            return;
        }
        if (type.matches(Mountable.GROUND_MOVEMENT.class.getSimpleName())){
            this.GROUND_MODE = value;
            return;
        }
        if (type.matches(Mountable.WATER_MOVEMENT.class.getSimpleName())){
            this.WATER_MODE = value;
            return;
        }
        if (type.matches(Mountable.FLYING_MOVEMENT.class.getSimpleName())){
            this.FLYING_MODE = value;
            return;
        }
        else {
            throw new RuntimeException("Invalid type provided, make an issue on the Mountables github tracker please.");
        }
    }
}
