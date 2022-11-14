package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.entity.Mountable;

import java.util.List;
import java.util.function.Supplier;

public class ServerUpdateMountModelPacket {

    private final int id;
    private final int position;

    public ServerUpdateMountModelPacket(int id, int index) {
        this.id = id;
        this.position = index;
    }

    public static ServerUpdateMountModelPacket decode(FriendlyByteBuf buf) {
        return new ServerUpdateMountModelPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ServerUpdateMountModelPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ServerLevel level = ctx.get().getSender().getLevel();
                    if (level.getEntity(packet.id) != null && level.getEntity(packet.id) instanceof Mountable mountable) {
                        final MountableData previousData = mountable.getMountableData();
                        List<MountableData> dataList = level.getServer().getRecipeManager().getAllRecipesFor(Mountables2Mod.MOUNTABLE_RECIPE_TYPE);
                        int index = dataList.indexOf(previousData);
                        if (index == -1) throw new RuntimeException("Previous MountableData was not found in Server Registry!");
                        index += packet.position;
                        if (index == -1) index = dataList.size() - 1;
                        if (index == dataList.size()) index = 0;
                        final MountableData newData = dataList.get(index);
                        mountable.loadMountableData(newData);
                        mountable.setEmissiveTexture(Mountable.TRANSPARENT_EMISSIVE_TEXTURE);
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
        return "ServerUpdateMountModelPacket{" +
                "id=" + id +
                ", position=" + position +
                '}';
    }
}
