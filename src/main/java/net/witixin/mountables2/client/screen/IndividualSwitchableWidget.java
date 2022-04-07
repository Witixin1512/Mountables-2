package net.witixin.mountables2.client.screen;

import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.network.PacketHandler;
import net.witixin.mountables2.network.server.ServerUpdateMountFreePacket;

import java.util.UUID;

public class IndividualSwitchableWidget extends SwitchableWidget{

    private final boolean isWater;
    private final UUID trackedID;


    public IndividualSwitchableWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation off, ResourceLocation on, String name, boolean isWater, UUID uuid, boolean selected) {
        super(pX, pY, pWidth, pHeight, off, on, name);
        this.trackedID = uuid;
        this.isWater = isWater;
        this.setEnabled(selected);
    }
    public void sendPacket(){
        PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFreePacket(trackedID, isWater, this.isEnabled()));
    }
    boolean isWater(){
        return isWater;
    }
    UUID getID(){
        return trackedID;
    }
}
