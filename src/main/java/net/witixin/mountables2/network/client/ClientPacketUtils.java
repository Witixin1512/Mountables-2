package net.witixin.mountables2.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.DistExecutor;
import net.witixin.mountables2.client.screen.CommandChipScreen;
import net.witixin.mountables2.data.MountableInfo;

import java.util.UUID;

public class ClientPacketUtils {

    public static DistExecutor.SafeRunnable openScreen(MountableInfo info){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new CommandChipScreen(new TextComponent("commandchipscreen"), info));
            }
        };
    }
}
