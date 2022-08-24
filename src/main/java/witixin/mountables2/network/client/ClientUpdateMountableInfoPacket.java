package witixin.mountables2.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import witixin.mountables2.data.MountableInfo;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientUpdateMountableInfoPacket {
    private final UUID id;
    private final String followMode;
    private final String freeMode;
    private final String groundMode;
    private final String waterMode;
    private final String flightMode;
    private final boolean waterState;
    private final boolean flightState;
    private final boolean canSwim;
    private final boolean canWalk;
    private final boolean canFly;

    private final boolean lockSwitch;

    public ClientUpdateMountableInfoPacket(MountableInfo info){
        this.id = info.uuid();
        this.followMode = info.followMode();
        this.freeMode = info.freeMode();
        this.groundMode = info.groundMode();
        this.waterMode = info.waterMode();
        this.flightMode = info.flightMode();
        this.waterState = info.waterState();
        this.flightState = info.flightState();
        this.canFly = info.canFly();
        this.canSwim = info.canSwim();
        this.canWalk = info.canWalk();
        this.lockSwitch = info.isSwitchLocked();
    }

    public static void handle(ClientUpdateMountableInfoPacket packet, Supplier<NetworkEvent.Context> ctx){
        MountableInfo info = new MountableInfo(packet.id, packet.followMode, packet.freeMode, packet.groundMode, packet.waterMode, packet.flightMode, packet.waterState, packet.flightState, packet.canSwim, packet.canWalk, packet.canFly, packet.lockSwitch);
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketUtils.setUpdateInfoOnClient(info))
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
        buf.writeBoolean(canSwim);
        buf.writeBoolean(canWalk);
        buf.writeBoolean(canFly);
        buf.writeBoolean(lockSwitch);
    }
    public static ClientUpdateMountableInfoPacket decode(FriendlyByteBuf buf){
        return new ClientUpdateMountableInfoPacket(new MountableInfo(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));
    }
}
