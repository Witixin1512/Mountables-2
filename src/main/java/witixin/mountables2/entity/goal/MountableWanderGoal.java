package witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import witixin.mountables2.entity.newmountable.Mountable;

;

public class MountableWanderGoal extends RandomStrollGoal {
    private final Mountable mountable;

    public MountableWanderGoal(Mountable mountable) {
        super(mountable, 0.5D, 100, false);
        this.mountable = mountable;
    }

    @Override
    public boolean canUse() {
//        return super.canUse() && mountable.getFollowMode().matches(Mountable.FOLLOW_TYPES.WANDER.name());
        return true;
    }
}
