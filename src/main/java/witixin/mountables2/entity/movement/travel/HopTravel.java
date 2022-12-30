package witixin.mountables2.entity.movement.travel;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MountMovement;

public class HopTravel implements MountMovement {

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {

        final double jumpStrength = mount.getAttributeValue(Attributes.JUMP_STRENGTH);

        if (mount.isOnGround() && !travelVector.add(Vec3.ZERO).equals(Vec3.ZERO)){
            travelVector = new Vec3(travelVector.x, jumpStrength, travelVector.z);
            mount.setOnGround(false);
        }
        else {
            //If we're jumping, drop speed by a bit
            travelVector = travelVector.multiply(0.1, 1.0, 0.1);
        }
        return travelVector;
    }
}
