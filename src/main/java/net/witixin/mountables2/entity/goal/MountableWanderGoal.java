package net.witixin.mountables2.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.witixin.mountables2.entity.Mountable;

public class MountableWanderGoal extends RandomStrollGoal {
    private Mountable mountable;

    public MountableWanderGoal(Mountable mountable) {
        super(mountable, 0.5D, 100, false);
        this.mountable = mountable;
    }

    @Override
    public boolean canUse(){
        return super.canUse() && mountable.getFollowMode().matches(Mountable.FOLLOW_TYPES.WANDER.name());
    }
}
