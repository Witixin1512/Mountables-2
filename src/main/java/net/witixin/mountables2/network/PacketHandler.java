package net.witixin.mountables2.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static int counter = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("mymodid", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init(){
        INSTANCE.registerMessage(counter++, ClientOpenScreenPacket.class, ClientOpenScreenPacket::encode, ClientOpenScreenPacket::decode, ClientOpenScreenPacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountFollowTypePacket.class, ServerUpdateMountFollowTypePacket::encode, ServerUpdateMountFollowTypePacket::decode, ServerUpdateMountFollowTypePacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountModelPacket.class, ServerUpdateMountModelPacket::encode, ServerUpdateMountModelPacket::decode, ServerUpdateMountModelPacket::handle);
        INSTANCE.registerMessage(counter++, ServerUpdateMountTexturePacket.class, ServerUpdateMountTexturePacket::encode, ServerUpdateMountTexturePacket::decode, ServerUpdateMountTexturePacket::handle);
    }
}