package witixin.mountables2.entity.goal;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import witixin.mountables2.entity.Mountable;

public class MountableFloatGoal extends FloatGoal {

    private Mountable mountable;

    public MountableFloatGoal(Mountable pMob) {
        super(pMob);
        this.mountable = pMob;
    }

    @Override
    public boolean canUse() {
        if (mountable.isVehicle()){
            return mountable.getWaterMode().matches(Mountable.WATER_MOVEMENT.FLOAT.name()) && super.canUse();
        }
        else {
            return super.canUse() && (!this.mountable.canFreeSwim());
        }
    }
}
