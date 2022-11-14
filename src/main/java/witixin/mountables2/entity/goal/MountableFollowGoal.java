package witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import witixin.mountables2.entity.Mountable;

public class MountableFollowGoal extends FollowOwnerGoal {

    private final Mountable mountable;

    public MountableFollowGoal(Mountable pTamable) {
        super(pTamable, 0.5, 6f, 0.5f, false);
        this.mountable = pTamable;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mountable.getControllingPassenger() == null && mountable.getFollowMode() == (Mountable.FOLLOW);
    }
}
