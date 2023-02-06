package witixin.mountables2.entity.movement.travel;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MountMovement;

public class FlyHopTravel implements MountMovement {

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {
        final double jumpStrength = mount.getAttributeValue(Attributes.JUMP_STRENGTH);

        if ((!travelVector.add(Vec3.ZERO).equals(Vec3.ZERO) || mount.getKeyStrokeMovement().spacebar()) && mount.getDeltaMovement().y <= 0.05) {
            travelVector = new Vec3(travelVector.x, jumpStrength, travelVector.z);
            mount.triggerAnim(Mountable.HOP_CONTROLLER, Mountable.JUMP_ANIMATION_NAME);
            mount.setOnGround(false);
            return travelVector;
        }
        else return Vec3.ZERO;
    }
}
