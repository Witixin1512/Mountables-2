package net.witixin.mountables2.entity;

import net.minecraft.world.entity.ai.control.MoveControl;

public class MountableControl extends MoveControl {

    protected final Mountable mountable;

    public MountableControl(Mountable pMob) {
        super(pMob);
        this.mountable = pMob;
    }
}
