package net.witixin.mountables2.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountTexturePacket {
    private final UUID id;
    private final int position;

    public ServerUpdateMountTexturePacket(UUID id, Integer index){
        this.id = id;
        this.position = index;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeInt(position);
    }
    public static ServerUpdateMountTexturePacket decode(FriendlyByteBuf buf){
        return new ServerUpdateMountTexturePacket(buf.readUUID(), buf.readInt());
    }
    public static void handle(ServerUpdateMountTexturePacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.id) != null && level.getEntity(packet.id) instanceof Mountable mountable){
                        mountable.setEmissiveTexture(packet.position);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
