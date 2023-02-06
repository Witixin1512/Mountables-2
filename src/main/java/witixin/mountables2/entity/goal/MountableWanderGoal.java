package witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import witixin.mountables2.entity.Mountable;

;

public class MountableWanderGoal extends RandomStrollGoal {
    private final Mountable mountable;

    public MountableWanderGoal(Mountable mountable) {
        super(mountable, 0.5D, 10, false);
        this.mountable = mountable;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mountable.getFollowMode() == Mountable.WANDER;
    }
}
