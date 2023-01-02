package witixin.mountables2.entity.movement.travel;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.entity.movement.MountMovement;

public class HopTravel implements MountMovement {

    boolean jumpOld = false;
    boolean airborne = false;

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {

        final double jumpStrength = mount.getAttributeValue(Attributes.JUMP_STRENGTH);
        final KeyStrokeMovement keyStrokeMovement = mount.getKeyStrokeMovement();
        if (mount.isOnGround() && (Mountable.isVectorNotZero(travelVector) || keyStrokeMovement.spacebar())){
            travelVector = new Vec3(travelVector.x, jumpStrength, travelVector.z);
            airborne = false;
        }
        else {
            //If we're jumping, drop speed by a bit
            if (!airborne && mount.canFly() && !jumpOld && keyStrokeMovement.spacebar() && !mount.level.isClientSide) {
                mount.setFlying(true);
                jumpOld = airborne =  true;
                return travelVector;
            }
            travelVector = travelVector.multiply(0.1, 1.0, 0.1);
        }
        jumpOld = keyStrokeMovement.spacebar();
        return travelVector;
    }
}
