package net.witixin.mountables2.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.witixin.mountables2.network.server.ServerUpdateMountTexturePacket;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class DefaultPacket {

    public abstract void encode(FriendlyByteBuf buf);

    public static DefaultPacket decode(FriendlyByteBuf buf) {
        return null;
    }

    public static void handle(ServerUpdateMountTexturePacket packet, Supplier<NetworkEvent.Context> ctx) {
    }
}
