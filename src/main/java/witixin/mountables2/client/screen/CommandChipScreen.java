package witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.widgets.ArrowSelectionWidget;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.newmountable.Mountable;

public class CommandChipScreen extends Screen {

    private static final ResourceLocation ARROW_LEFT = Mountables2Mod.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Mountables2Mod.rl("gui/mountable_right_button.png");
    public Mountable mount;
    public final int entityId;

    public CommandChipScreen(int mountId) {
        super(new TextComponent("commandchipscreen"));
        this.entityId = mountId;
    }


    @Override
    protected void init() {
        super.init();
        if (minecraft.level.getEntity(entityId) instanceof Mountable mount)
            this.mount = mount;
        else throw new NullPointerException("given entity was not a mount !");

        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        final ArrowSelectionWidget MODEL_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget MODEL_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

        final ArrowSelectionWidget TEXTURE_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget TEXTURE_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);


        //TODO fix follow buttons
        final SwitchableWidget WANDER = new SwitchableWidget(0, 40, 100, 20, I18n.get("gui.mountables2.chip.wander"), "wander", pButton -> {
        });
        final SwitchableWidget STAY = new SwitchableWidget(0, 80, 100, 20, I18n.get("gui.mountables2.chip.stay"), "stay", pButton -> {
        });
        final SwitchableWidget FOLLOW = new SwitchableWidget(0, 120, 100, 20, I18n.get("gui.mountables2.chip.follow"), "follow", pButton -> {
        });


        this.addRenderableWidget(new LinkedSwitchableWidget((posX - 128) / 2 + 10, (posY - 256) / 2, 40, 40, ".*", WANDER, STAY, FOLLOW));

//        if (!mount.getLockSwitch()) {
//            this.addRenderableWidget(new LinkedArrowWidget((posX - 128) / 2 + 10, (posY - 256) / 2 + 160, 40, 40, MODEL_LEFT, MODEL_RIGHT, I18n.get("gui.mountables2.chip.model")));
//        }
//
//        this.addRenderableWidget(new LinkedArrowWidget((posX - 128) / 2 + 10, (posY - 256) / 2 + 180 - (mount.getLockSwitch() ? 20 : 0), 300, 60, TEXTURE_LEFT, TEXTURE_RIGHT, I18n.get("gui.mountables2.chip.texture")));

        this.addRenderableWidget(new SwitchableWidget(((posX - 128) / 2 + 10), (posY - 256) / 2 + 200, 100, 20, I18n.get("gui.mountables2.ai.ai"), "AI", pButton -> {
            minecraft.setScreen(new MountableAIScreen(entityId));
        }));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
//        if (this.getTitle().getString().matches("commandchipscreen") && AI_WIDGET != null && AI_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)) {
//            PacketHandler.INSTANCE.sendToServer(new ServerRequestMountableInfoPacket(trackedMountable));
//            Minecraft.getInstance().setScreen(new MountableAIScreen(new MountableInfo(trackedMountable, followMode, NON_RIDER_MODE, GROUND_MODE, WATER_MODE, FLYING_MODE, waterState, flightState, canSwim, canWalk, canFly, lockSwitch)));
//            return true;
//        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

}
