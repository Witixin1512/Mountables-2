package net.witixin.mountables2.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.entity.Mountable;
import net.witixin.mountables2.network.ServerUpdateMountFollowTypePacket;
import net.witixin.mountables2.network.ServerUpdateMountModelPacket;
import net.witixin.mountables2.network.ServerUpdateMountTexturePacket;

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

    private static final ResourceLocation BIG_BUTTON_OFF = Reference.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Reference.rl("gui/mountable_command_button_selected.png");
    private static final ResourceLocation ARROW_LEFT = Reference.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Reference.rl("gui/mountable_right_button.png");

    private final SwitchableWidget WANDER = new SwitchableWidget(0, 40, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.WANDER.name());
    private final SwitchableWidget STAY = new SwitchableWidget(0, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.STAY.name());
    private final SwitchableWidget FOLLOW = new SwitchableWidget(0, 120, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.FOLLOW.name());

    private SwitchableWidget AI_WIDGET;

    private final ArrowSelectionWidget MODEL_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
    private final ArrowSelectionWidget MODEL_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

    private final ArrowSelectionWidget TEXTURE_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
    private final ArrowSelectionWidget TEXTURE_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);


    public CommandChipScreen(Component comp, UUID mountableID, String followMode, String freeMode, String groundMode, String waterMode, String flyingMode, boolean waterState, boolean flightState) {
        super(comp);
        this.trackedMountable = mountableID;
        this.followMode = followMode;
        this.NON_RIDER_MODE = freeMode;
        this.GROUND_MODE = groundMode;
        this.WATER_MODE = waterMode;
        this.FLYING_MODE = flyingMode;
        this.waterState = waterState;
        this.flightState = flightState;
    }


    @Override
    protected void init() {
        this.addRenderableWidget(new LinkedSwitchableWidget((this.width -128)/2 + 10, (this.height - 256)/2, 300, 60, trackedMountable, followMode, ServerUpdateMountFollowTypePacket.class, "", WANDER, STAY, FOLLOW));
        this.addRenderableWidget(new LinkedArrowWidget((this.width -128)/2 + 10, (this.height - 256)/2 + 160 , 300, 60, trackedMountable, MODEL_LEFT, MODEL_RIGHT, ServerUpdateMountModelPacket.class, "MODEL"));
        this.addRenderableWidget(new LinkedArrowWidget((this.width -128)/2 + 10, (this.height - 256)/2 + 180 , 300, 60, trackedMountable, TEXTURE_LEFT, TEXTURE_RIGHT, ServerUpdateMountTexturePacket.class, "TEXTURE"));
        AI_WIDGET = this.addRenderableWidget(new SwitchableWidget(((this.width - 128) / 2 + 10),(this.height -256 )/2 + 200 , 100, 20,  BIG_BUTTON_OFF, BIG_BUTTON_ON, "AI"));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.getTitle().getString().matches("commandchipscreen") && AI_WIDGET != null && AI_WIDGET.mouseClicked(pMouseX, pMouseY, pButton)){
            Minecraft.getInstance().setScreen(new MountableAIScreen(trackedMountable, followMode, NON_RIDER_MODE, GROUND_MODE, WATER_MODE, FLYING_MODE, waterState, flightState));
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    void setFollowMode(String s){
        this.followMode = s;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
