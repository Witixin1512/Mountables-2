package net.witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.witixin.mountables2.entity.Mountable;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerUpdateMountModelPacket  {

    private final UUID id;
    private final int position;

    public ServerUpdateMountModelPacket(UUID id, Integer index){
        this.id = id;
        this.position = index;
    }


    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeInt(position);
    }
    public static ServerUpdateMountModelPacket decode(FriendlyByteBuf buf){
        return new ServerUpdateMountModelPacket(buf.readUUID(), buf.readInt());
    }

    public static void handle(ServerUpdateMountModelPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.id) != null && level.getEntity(packet.id) instanceof Mountable mountable){
                        final int sum = mountable.getModelPosition() + packet.position;
                        mountable.loadMountableData(sum);
                        mountable.resetDefaultDataParameters();
                        mountable.setModelPosition(sum);
                        mountable.setEmissiveTexture(0);
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
