package net.witixin.mountables2.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientOpenScreenPacket {

    private final UUID id;
    private final String followMode;
    private final String freeMode;
    private final String groundMode;
    private final String waterMode;
    private final String flightMode;
    private final boolean waterState;
    private final boolean flightState;

    public ClientOpenScreenPacket(UUID uuid, String followMode, String freeMode, String groundMode, String waterMode, String flightMode, boolean waterState, boolean flightState){
        this.id = uuid;
        this.followMode = followMode;
        this.freeMode = freeMode;
        this.groundMode = groundMode;
        this.waterMode = waterMode;
        this.flightMode = flightMode;
        this.waterState = waterState;
        this.flightState = flightState;
    }
    public static void handle(ClientOpenScreenPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketUtils.openScreen(packet.id, packet.followMode, packet.freeMode, packet.groundMode, packet.waterMode, packet.flightMode, packet.waterState, packet.flightState))
        );
        ctx.get().setPacketHandled(true);
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeUUID(id);
        buf.writeUtf(followMode);
        buf.writeUtf(freeMode);
        buf.writeUtf(groundMode);
        buf.writeUtf(waterMode);
        buf.writeUtf(flightMode);
        buf.writeBoolean(waterState);
        buf.writeBoolean(flightState);
    }
    public static ClientOpenScreenPacket decode(FriendlyByteBuf buf){
        return new ClientOpenScreenPacket(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
    }
}
