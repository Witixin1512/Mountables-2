package net.witixin.mountables2.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientOpenScreenPacket {

    private final UUID id;
    private final String followMode;
    private final CompoundTag tag;
    public ClientOpenScreenPacket(String followMode, UUID uuid, CompoundTag tag){
        this.id = uuid;
        this.followMode = followMode;
        //TODO make list available to client and also sync with server buttons
        CompoundTag list = new CompoundTag();
        StringBuilder builder = new StringBuilder();
        //stringList.iterator().forEachRemaining(string -> builder.append(":" + string));
        this.tag = list;
    }
    public static void handle(ClientOpenScreenPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketUtils.openScreen(packet.id, packet.followMode, packet.tag))
        );
        ctx.get().setPacketHandled(true);
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeUtf(followMode);
        buf.writeUUID(id);
        buf.writeNbt(tag);
    }
    public static ClientOpenScreenPacket decode(FriendlyByteBuf buf){
        return new ClientOpenScreenPacket(buf.readUtf(), buf.readUUID(), buf.readNbt());
    }
}
