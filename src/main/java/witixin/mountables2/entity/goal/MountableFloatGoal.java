package witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MountTravel;

public class MountableFloatGoal extends FloatGoal {

    private final Mountable mountable;

    public MountableFloatGoal(Mountable pMob) {
        super(pMob);
        this.mountable = pMob;
    }

    @Override
    public boolean canUse() {
        if (mountable.canSwim() && !mountable.isVehicle()) return mountable.getMajor() == MountTravel.Major.SWIM && mountable.getMinorMovement(MountTravel.Major.SWIM) == MountTravel.Minor.FLOAT;
        return true;
    }
}
