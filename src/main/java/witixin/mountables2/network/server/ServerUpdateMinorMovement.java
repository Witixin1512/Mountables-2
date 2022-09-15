package witixin.mountables2.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.entity.newmountable.Mountable;
import witixin.mountables2.entity.newmountable.movement.MountTravel;

import java.util.function.Supplier;

public class ServerUpdateMinorMovement {
    private final int entityId;
    private final MountTravel.Minor minor;
    private final MountTravel.Major major;

    public ServerUpdateMinorMovement(int id, MountTravel.Major major, MountTravel.Minor minor) {
        this.entityId = id;
        this.minor = minor;
        this.major = major;
    }

    public static void handle(ServerUpdateMinorMovement packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
                    var level = context.get().getSender().level;
                    if (level.getEntity(packet.entityId) instanceof Mountable mount)
                        mount.setMinorMovement(packet.major, packet.minor);
                }
        );
        context.get().setPacketHandled(true);
    }

    public static ServerUpdateMinorMovement decode(FriendlyByteBuf buf) {
        return new ServerUpdateMinorMovement(buf.readInt(), buf.readEnum(MountTravel.Major.class), buf.readEnum(MountTravel.Minor.class));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeEnum(major);
        buf.writeEnum(minor);
    }

}
