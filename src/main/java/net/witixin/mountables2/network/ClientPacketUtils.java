package net.witixin.mountables2.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.DistExecutor;
import net.witixin.mountables2.client.screen.CommandChipScreen;

import java.util.UUID;

public class ClientPacketUtils {

    public static DistExecutor.SafeRunnable openScreen(UUID uuid, String followMode, String freeMode, String groundMode, String waterMode, String flightMode, boolean waterState, boolean flightState){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new CommandChipScreen(new TextComponent("commandchipscreen"), uuid, followMode, freeMode, groundMode, waterMode, flightMode, waterState, flightState));
            }
        };
    }
}
