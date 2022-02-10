package net.witixin.mountables2.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.DistExecutor;
import net.witixin.mountables2.client.screen.CommandChipScreen;

import java.util.UUID;

public class ClientPacketUtils {

    public static DistExecutor.SafeRunnable openScreen(UUID uuid, String followMode, CompoundTag tag){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new CommandChipScreen(uuid, followMode, tag));
            }
        };
    }
}
