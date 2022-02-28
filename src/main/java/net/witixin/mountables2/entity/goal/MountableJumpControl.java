package net.witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.control.JumpControl;
import net.witixin.mountables2.entity.Mountable;

public class MountableJumpControl extends JumpControl {

    protected final Mountable mountable;

    public MountableJumpControl(Mountable pMob) {
        super(pMob);
        this.mountable = pMob;
    }
}
