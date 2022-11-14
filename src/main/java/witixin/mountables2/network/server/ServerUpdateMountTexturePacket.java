package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.entity.Mountable;

import java.util.List;
import java.util.function.Supplier;

public class ServerUpdateMountTexturePacket {
    private final int id;
    private final int position;

    public ServerUpdateMountTexturePacket(int id, Integer index) {
        this.id = id;
        this.position = index;
    }

    public static ServerUpdateMountTexturePacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountTexturePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ServerUpdateMountTexturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.id) != null && level.getEntity(packet.id) instanceof Mountable mountable) {
                        final MountableData mountableData = mountable.getMountableData();
                        List<String> emissiveTextureList = mountableData.emissiveTextures();
                        if (emissiveTextureList.isEmpty()) return;
                        // "transparent" will never be on the list and return -1
                        int index = emissiveTextureList.indexOf(mountable.getEmissiveTexture());
                        index += packet.position;
                        if (index == -1 || index == emissiveTextureList.size()){
                            mountable.setEmissiveTexture(Mountable.TRANSPARENT_EMISSIVE_TEXTURE);
                        }
                        else if (index == -2){
                            mountable.setEmissiveTexture(emissiveTextureList.get(emissiveTextureList.size() - 1));
                        }
                        else {
                            mountable.setEmissiveTexture(emissiveTextureList.get(index));
                        }
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(position);
    }

    @Override
    public String toString() {
        return "ServerUpdateMountTexturePacket{" +
                "id=" + id +
                ", position=" + position +
                '}';
    }
}
