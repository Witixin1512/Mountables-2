//package witixin.mountables2.data;
//
//import net.minecraft.network.FriendlyByteBuf;
//
//import java.util.UUID;
//
//public record MountableInfo(UUID uuid, String followMode, String freeMode, String groundMode, String waterMode, String flightMode, boolean waterState, boolean flightState, boolean canSwim, boolean canWalk, boolean canFly, boolean isSwitchLocked) {
//
//    void fromBuf(FriendlyByteBuf buf) {
//        this.uuid = buf.readUUID();
//    }
//
//    void toBuf(FriendlyByteBuf buf) {

// is this deletable? What has superseeded it?
//    }
//}
