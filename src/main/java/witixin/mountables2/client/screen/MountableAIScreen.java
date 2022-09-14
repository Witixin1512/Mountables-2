package witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import witixin.mountables2.client.screen.widgets.IndividualSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.newmountable.movement.MountTravel;
import witixin.mountables2.entity.newmountable.movement.MovementRegistry;

public class MountableAIScreen extends CommandChipScreen {

    private SwitchableWidget SCREEN_CHANGE_WIDGET;
    private IndividualSwitchableWidget SWIM_WIDGET;
    private IndividualSwitchableWidget FLY_WIDGET;


    public MountableAIScreen(int entityId) {
        super(entityId);
    }

    @Override
    protected void init() {
        super.init();
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        //FREE_MODE
//        final SwitchableWidget WALK = new SwitchableWidget(-140, 50, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.walk"), Mountable.NON_RIDER.WALK.name());
//        final SwitchableWidget SLOW_WALK = new SwitchableWidget(-140, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.slow_walk"), Mountable.NON_RIDER.SLOW_WALK.name());
//        final SwitchableWidget JUMP = new SwitchableWidget(-140, 110, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.jump"), Mountable.NON_RIDER.JUMP.name());

        int index = 0;
        int majorID = 0;
        for (MountTravel.Major major : MountTravel.Major.values()) {
            //todo skip major if mount cannot fly or swim
            for (MountTravel travel : MovementRegistry.INSTANCE.getMajorMovement(major)) {
                addRenderableWidget(
                        new SwitchableWidget(
                                posX / 2 + (-100 + (100 * majorID)), 50 + (30 * index),
                                80, 20,
                                I18n.get(String.format("gui.mountables2.ai.%s", travel.getMinor().name().toLowerCase())),
                                travel.getMinor().name(),
                                pButton -> {
                                    this.mount.setMinorMovement(travel.getMajor(), travel.getMinor());
                                    //TODO send packet to id with minor version
                                }));
                index++;
            }
            majorID++;
            index = 0;
        }

//        if (this.canSwim) {
//            this.SWIM_WIDGET = this.addRenderableWidget(new IndividualSwitchableWidget((posX - 128) / 2 - 140, (posY - 256) / 2 + 140, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.swim"), true, trackedMountable, waterState));
//            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) / 2, (posY - 256) / 2, 100, 60, trackedMountable, this.WATER_MODE, ServerUpdateMountAIPacket.class, Mountable.WATER_MOVEMENT.class.getSimpleName(), FLOAT, SINK, WATER_SWIM));
//        }
//        if (this.canWalk) {
//            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) / 2, (posY - 256) / 2, 100, 60, trackedMountable, this.NON_RIDER_MODE, ServerUpdateMountAIPacket.class, Mountable.NON_RIDER.class.getSimpleName(), WALK, SLOW_WALK, JUMP));
//            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) / 2, (posY - 256) / 2, 100, 80, trackedMountable, this.GROUND_MODE, ServerUpdateMountAIPacket.class, Mountable.GROUND_MOVEMENT.class.getSimpleName(), NONE, GROUND_SLOW_WALK, GROUND_WALK, HOP));
//        }
//        if (this.canFly) {
//            this.FLY_WIDGET = this.addRenderableWidget(new IndividualSwitchableWidget((posX - 128) / 2 - 140, (posY - 256) / 2 + 170, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.fly"), false, trackedMountable, flightState));
//            this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) / 2, (posY - 256) / 2, 100, 60, trackedMountable, this.FLYING_MODE, ServerUpdateMountAIPacket.class, Mountable.FLYING_MOVEMENT.class.getSimpleName(), AIR_NONE, FLIGHT, AIR_HOP));
//        }
        this.SCREEN_CHANGE_WIDGET = addRenderableWidget(new SwitchableWidget((posX / 2 - 50), posY / 2 + 72, 100, 20, I18n.get("entity.mountables2.mountable_entity"), "Mountable", pButton -> {
            minecraft.setScreen(new CommandChipScreen(entityId));
        }));
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
//        if (canSwim || canFly || canWalk) {
        String aiText = I18n.get("gui.mountables2.ai.ai");
        Minecraft.getInstance().font.drawShadow(pPoseStack, aiText, (posX - 128) / 2 - 90 - Minecraft.getInstance().font.width(aiText) / 2, (posY - 256) / 2 + 30, 0xffffff);
//        }
//        if (canSwim) {
        String swimText = I18n.get("gui.mountables2.ai.swim");
        Minecraft.getInstance().font.drawShadow(pPoseStack, swimText, (posX - 128) / 2 + 110 - Minecraft.getInstance().font.width(swimText) / 2, (posY - 256) / 2 + 30, 0xffffff);
//        }
//        if (canWalk) {
        String walkText = I18n.get("gui.mountables2.ai.walk");
        Minecraft.getInstance().font.drawShadow(pPoseStack, walkText, (posX - 128) / 2 + 10 - Minecraft.getInstance().font.width(walkText) / 2, (posY - 256) / 2 + 30, 0xffffff);
//        }
//        if (canFly) {
        String flyText = I18n.get("gui.mountables2.ai.fly");
        Minecraft.getInstance().font.drawShadow(pPoseStack, flyText, (posX - 128) / 2 + 210 - Minecraft.getInstance().font.width(flyText) / 2, (posY - 256) / 2 + 30, 0xffffff);
//        }
    }
}
