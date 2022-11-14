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

        //set flying mod if the mount is airborne, has not landed again, and the key has been let go
        if (!mount.isOnGround() && airborne && !jumpOld && !mount.level.isClientSide) {
            mount.setFlying(true);
        }
        //only rejump if the key has been let go

        if (mount.isOnGround()){
            if (airborne){
                airborne = false;
            }
            if (mount.getKeyStrokeMovement().spacebar() && !airborne){
                jumpOld = airborne = true;
                mod = new Vec3(mod.x, mount.getAttributeValue(Attributes.JUMP_STRENGTH), mod.z);
                mount.setOnGround(false);
            }
        }
        jumpOld = mount.getKeyStrokeMovement().spacebar();
        return mod;
    }

}
