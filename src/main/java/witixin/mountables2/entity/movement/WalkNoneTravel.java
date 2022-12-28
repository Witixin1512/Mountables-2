package witixin.mountables2.entity.movement;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;

public class WalkNoneTravel implements MountMovement {

    boolean airborne = false;
    boolean jumpOld = false;

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {
        Vec3 mod = travelVector;
        final KeyStrokeMovement keyStrokeMovement = mount.getKeyStrokeMovement();
        //set flying mod if the mount is airborne, has not landed again, and the key has been let go
        if (!mount.isOnGround() && airborne && !jumpOld && !mount.level.isClientSide && keyStrokeMovement.spacebar()) {
            mount.setFlying(true);
            return mod;
        }
        //only rejump if the key has been let go

        if (mount.isOnGround()){
            if (airborne){
                airborne = false;
            }
            if (mount.getKeyStrokeMovement().spacebar() && !airborne){
                jumpOld = airborne = true;
                mod = new Vec3(0, mount.getAttributeValue(Attributes.JUMP_STRENGTH), 0);
                mount.setOnGround(false);
            }
        }
        jumpOld = keyStrokeMovement.spacebar();
        return mod;
    }

}
