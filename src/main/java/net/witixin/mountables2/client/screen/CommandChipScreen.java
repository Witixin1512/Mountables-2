package net.witixin.mountables2.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.client.screen.widgets.ArrowSelectionWidget;
import net.witixin.mountables2.client.screen.widgets.LinkedArrowWidget;
import net.witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import net.witixin.mountables2.client.screen.widgets.SwitchableWidget;
import net.witixin.mountables2.data.MountableInfo;
import net.witixin.mountables2.entity.Mountable;
import net.witixin.mountables2.network.PacketHandler;
import net.witixin.mountables2.network.server.ServerRequestMountableInfoPacket;
import net.witixin.mountables2.network.server.ServerUpdateMountFollowTypePacket;
import net.witixin.mountables2.network.server.ServerUpdateMountModelPacket;
import net.witixin.mountables2.network.server.ServerUpdateMountTexturePacket;

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

    private static final ResourceLocation BIG_BUTTON_OFF = Reference.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Reference.rl("gui/mountable_command_button_selected.png");
    private static final ResourceLocation ARROW_LEFT = Reference.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Reference.rl("gui/mountable_right_button.png");

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
    }


    @Override
    protected void init() {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        final ArrowSelectionWidget MODEL_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget MODEL_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

        final ArrowSelectionWidget TEXTURE_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        final ArrowSelectionWidget TEXTURE_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);


        final SwitchableWidget WANDER = new SwitchableWidget(0, 40, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.WANDER.name());
        final SwitchableWidget STAY = new SwitchableWidget(0, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.STAY.name());
        final SwitchableWidget FOLLOW = new SwitchableWidget(0, 120, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.FOLLOW.name());


        this.addRenderableWidget(new LinkedSwitchableWidget((posX -128)/2 + 10, (posY - 256)/2, 300, 60, trackedMountable, followMode, ServerUpdateMountFollowTypePacket.class, "", WANDER, STAY, FOLLOW));

        this.addRenderableWidget(new LinkedArrowWidget((posX -128)/2 + 10, (posY - 256)/2 + 160 , 300, 60, trackedMountable, MODEL_LEFT, MODEL_RIGHT, ServerUpdateMountModelPacket.class, "MODEL"));

        this.addRenderableWidget(new LinkedArrowWidget((posX -128)/2 + 10, (posY - 256)/2 + 180 , 300, 60, trackedMountable, TEXTURE_LEFT, TEXTURE_RIGHT, ServerUpdateMountTexturePacket.class, "TEXTURE"));

        AI_WIDGET = this.addRenderableWidget(new SwitchableWidget(((posX - 128) / 2 + 10),(posY -256 )/2 + 200 , 100, 20,  BIG_BUTTON_OFF, BIG_BUTTON_ON, "AI"));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.getTitle().getString().matches("commandchipscreen") && AI_WIDGET != null && AI_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            PacketHandler.INSTANCE.sendToServer(new ServerRequestMountableInfoPacket(trackedMountable));
            Minecraft.getInstance().setScreen(new MountableAIScreen(new MountableInfo(trackedMountable, followMode, NON_RIDER_MODE, GROUND_MODE, WATER_MODE, FLYING_MODE, waterState, flightState, canSwim, canWalk, canFly)));
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
