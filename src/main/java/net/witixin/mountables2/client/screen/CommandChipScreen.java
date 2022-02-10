package net.witixin.mountables2.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.client.screen.base.ArrowSelectionWidget;
import net.witixin.mountables2.client.screen.base.SwitchableWidget;
import net.witixin.mountables2.entity.Mountable;
import net.witixin.mountables2.network.ServerUpdateMountModelPacket;
import net.witixin.mountables2.network.ServerUpdateMountTexturePacket;

import java.util.UUID;

public class CommandChipScreen extends Screen {

    private final UUID trackedMountable;
    private final String followModeOld;
    private final CompoundTag tag;

    private static final ResourceLocation BIG_BUTTON_OFF = Reference.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Reference.rl("gui/mountable_command_button_selected.png");
    private static final ResourceLocation ARROW_LEFT = Reference.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Reference.rl("gui/mountable_right_button.png");

    private final SwitchableWidget WANDER = new SwitchableWidget(0, 40, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.WANDER.name());
    private final SwitchableWidget STAY = new SwitchableWidget(0, 80, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.STAY.name());
    private final SwitchableWidget FOLLOW = new SwitchableWidget(0, 120, 100, 20, BIG_BUTTON_OFF, BIG_BUTTON_ON, Mountable.FOLLOW_TYPES.FOLLOW.name());

    private final ArrowSelectionWidget MODEL_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
    private final ArrowSelectionWidget MODEL_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

    private final ArrowSelectionWidget TEXTURE_LEFT = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
    private final ArrowSelectionWidget TEXTURE_RIGHT = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

    public CommandChipScreen(UUID mountableID, String followMode, CompoundTag tag) {
        super(new TextComponent("commandchipscreen"));
        this.trackedMountable = mountableID;
        this.followModeOld = followMode;
        this.tag = tag;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new LinkedSwitchableWidget((this.width -128)/2, (this.height - 256)/2, 300, 60, trackedMountable, followModeOld,   WANDER, STAY, FOLLOW));
        this.addRenderableWidget(new LinkedArrowWidget((this.width -128)/2, (this.height - 256)/2 + 160 , 300, 60, trackedMountable, MODEL_LEFT, MODEL_RIGHT, ServerUpdateMountModelPacket.class, "MODEL"));
        this.addRenderableWidget(new LinkedArrowWidget((this.width -128)/2, (this.height - 256)/2 + 180 , 300, 60, trackedMountable, TEXTURE_LEFT, TEXTURE_RIGHT, ServerUpdateMountTexturePacket.class, "TEXTURE"));
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
