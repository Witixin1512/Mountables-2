package witixin.mountables2.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.widgets.ArrowSelectionWidget;
import witixin.mountables2.client.screen.widgets.LinkedArrowWidget;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.data.MountableInfo;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerRequestMountableInfoPacket;
import witixin.mountables2.network.server.ServerUpdateMountFollowTypePacket;
import witixin.mountables2.network.server.ServerUpdateMountModelPacket;
import witixin.mountables2.network.server.ServerUpdateMountTexturePacket;

import java.util.UUID;

public class CommandChipScreen extends Screen {

    protected final UUID trackedMountable;
    protected String followMode;
    protected String NON_RIDER_MODE;
    protected String GROUND_MODE;
    protected String WATER_MODE;
    protected String FLYING_MODE;
    protected boolean waterState;
    protected boolean flightState;

    protected final boolean canFly;
    protected final boolean canSwim;
    protected final boolean canWalk;

    protected final boolean lockSwitch;

    private static final ResourceLocation BIG_BUTTON_OFF = Mountables2Mod.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Mountables2Mod.rl("gui/mountable_command_button_selected.png");
    private static final ResourceLocation ARROW_LEFT = Mountables2Mod.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Mountables2Mod.rl("gui/mountable_right_button.png");

    public static MountableInfo mountableInfo;

    private SwitchableWidget AI_WIDGET;

    public CommandChipScreen(MountableInfo info) {
        super(new TextComponent("commandchipscreen"));
        this.trackedMountable = info.uuid();
        this.followMode = info.followMode();
        this.NON_RIDER_MODE = info.freeMode();
        this.GROUND_MODE = info.groundMode();
        this.WATER_MODE = info.waterMode();
        this.FLYING_MODE = info.flightMode();
        this.waterState = info.waterState();
        this.flightState = info.flightState();
        this.canFly = info.canFly();
        this.canWalk = info.canWalk();
        this.canSwim = info.canSwim();
        this.lockSwitch = info.isSwitchLocked();
    }


    @Override
    protected void init() {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        final ArrowSelectionWidget MODEL_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget MODEL_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

        final ArrowSelectionWidget TEXTURE_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget TEXTURE_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);


        final SwitchableWidget WANDER = new SwitchableWidget(0, 40, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.chip.wander"), Mountable.FOLLOW_TYPES.WANDER.name());
        final SwitchableWidget STAY = new SwitchableWidget(0, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.chip.stay"), Mountable.FOLLOW_TYPES.STAY.name());
        final SwitchableWidget FOLLOW = new SwitchableWidget(0, 120, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.chip.follow"), Mountable.FOLLOW_TYPES.FOLLOW.name());



        this.addRenderableWidget(new LinkedSwitchableWidget((posX -128)/2 + 10, (posY - 256)/2, 300, 60, trackedMountable, followMode, ServerUpdateMountFollowTypePacket.class, "", WANDER, STAY, FOLLOW));

        if (!lockSwitch){
            this.addRenderableWidget(new LinkedArrowWidget((posX -128)/2 + 10, (posY - 256)/2 + 160 , 300, 60, trackedMountable, MODEL_LEFT, MODEL_RIGHT, ServerUpdateMountModelPacket.class, I18n.get("gui.mountables2.chip.model")));
        }

        this.addRenderableWidget(new LinkedArrowWidget((posX -128)/2 + 10, (posY - 256)/2 + 180 - (lockSwitch ? 20 : 0)   , 300, 60, trackedMountable, TEXTURE_LEFT, TEXTURE_RIGHT, ServerUpdateMountTexturePacket.class, I18n.get("gui.mountables2.chip.texture")));

        AI_WIDGET = this.addRenderableWidget(new SwitchableWidget(((posX - 128) / 2 + 10),(posY -256 )/2 + 200 , 100, 20,  BIG_BUTTON_OFF, BIG_BUTTON_ON, I18n.get("gui.mountables2.ai.ai"), "AI"));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.getTitle().getString().matches("commandchipscreen") && AI_WIDGET != null && AI_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            PacketHandler.INSTANCE.sendToServer(new ServerRequestMountableInfoPacket(trackedMountable));
            Minecraft.getInstance().setScreen(new MountableAIScreen(new MountableInfo(trackedMountable, followMode, NON_RIDER_MODE, GROUND_MODE, WATER_MODE, FLYING_MODE, waterState, flightState, canSwim, canWalk, canFly, lockSwitch)));
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    public void setFollowMode(String s){
        this.followMode = s;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
