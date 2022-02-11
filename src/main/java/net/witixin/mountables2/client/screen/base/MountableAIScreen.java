package net.witixin.mountables2.client.screen.base;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;

import java.util.UUID;

public class MountableAIScreen extends Screen {

    private final UUID trackedMountable;
    private final String followModeOld;

    private static final ResourceLocation BIG_BUTTON_OFF = Reference.rl("gui/mountable_command_button.png");
    private static final ResourceLocation BIG_BUTTON_ON = Reference.rl("gui/mountable_command_button_selected.png");

    public MountableAIScreen(UUID mountableID, String followMode) {
        super(new TextComponent("mountable_screen"));
        this.trackedMountable = mountableID;
        this.followModeOld = followMode;
    }
}
