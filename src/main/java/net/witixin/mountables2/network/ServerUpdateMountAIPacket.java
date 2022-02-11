package net.witixin.mountables2.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountAIPacket{

    private final UUID ENTITY_ID;
    private final String AI_MODE;

    public ServerUpdateMountAIPacket(UUID entity_id, String ai_mode){
        this.AI_MODE = ai_mode;
        this.ENTITY_ID = entity_id;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(ENTITY_ID);
        buf.writeUtf(AI_MODE);
    }
    public static ServerUpdateMountAIPacket decode(FriendlyByteBuf buf){
        return new ServerUpdateMountAIPacket(buf.readUUID(), buf.readUtf());
    }

    public static void handle(ServerUpdateMountAIPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.ENTITY_ID) != null && level.getEntity(packet.ENTITY_ID) instanceof Mountable mountable){
                        mountable.setAIMode(packet.AI_MODE);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
