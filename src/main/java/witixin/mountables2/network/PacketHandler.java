package witixin.mountables2.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.network.server.*;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mountables2Mod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int counter = 0;

    public static void init() {
        INSTANCE.registerMessage(counter++, ServerUpdateMinorMovement.class, ServerUpdateMinorMovement::encode, ServerUpdateMinorMovement::decode, ServerUpdateMinorMovement::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountFollowTypePacket.class, ServerUpdateMountFollowTypePacket::encode, ServerUpdateMountFollowTypePacket::decode, ServerUpdateMountFollowTypePacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountModelPacket.class, ServerUpdateMountModelPacket::encode, ServerUpdateMountModelPacket::decode, ServerUpdateMountModelPacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountTexturePacket.class, ServerUpdateMountTexturePacket::encode, ServerUpdateMountTexturePacket::decode, ServerUpdateMountTexturePacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountAIPacket.class, ServerUpdateMountAIPacket::encode, ServerUpdateMountAIPacket::decode, ServerUpdateMountAIPacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountFreePacket.class, ServerUpdateMountFreePacket::encode, ServerUpdateMountFreePacket::decode, ServerUpdateMountFreePacket::handle);
        INSTANCE.registerMessage(counter++, ServerRequestMountableInfoPacket.class, ServerRequestMountableInfoPacket::encode, ServerRequestMountableInfoPacket::decode, ServerRequestMountableInfoPacket::handle);
        INSTANCE.registerMessage(counter++, ServerHandleKeyPressMovement.class, ServerHandleKeyPressMovement::encode, ServerHandleKeyPressMovement::new, ServerHandleKeyPressMovement::handle);
    }

}
