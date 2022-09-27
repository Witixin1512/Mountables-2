package witixin.mountables2.entity.newmountable.movement;

import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.newmountable.Mountable;

public class WalkNoneTravel implements MountMovement {

    boolean airborne;
    boolean jumpOld = false;

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {
        Vec3 mod = travelVector;

        //set flying mod if the mount is airborne, has not landed again, and the key has been let go
        if (!mount.isOnGround() && airborne && !jumpOld) {
            mount.setFlying(true);
        }
        //only rejump if the key has been let go
        if (mount.isOnGround() && !mount.getKeyStrokeMovement().jump() && airborne) {
            airborne = false;
        }
        //impulse jump
        if (mount.isOnGround() && mount.getKeyStrokeMovement().jump() && !airborne) {
            jumpOld = airborne = true;
            mod = new Vec3(mod.x, 1.2, mod.z);
            mount.setOnGround(false);
        }

        jumpOld = mount.getKeyStrokeMovement().jump();

        return mod;
    }
}
