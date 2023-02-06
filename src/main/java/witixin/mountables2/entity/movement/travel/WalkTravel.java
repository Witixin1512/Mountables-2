package witixin.mountables2.entity.movement.travel;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.entity.movement.MountMovement;

public class WalkTravel implements MountMovement {

    boolean airborne = false;
    boolean jumpOld = false;

    private final double speedCoeficient;

    public WalkTravel(double coeficient) {
        this.speedCoeficient = coeficient;
    }

    @Override
    public Vec3 travel(Mountable mount, Vec3 travelVector) {
        Vec3 mod = travelVector;
        final KeyStrokeMovement keyStrokeMovement = mount.getKeyStrokeMovement();
        //set flying mod if the mount is airborne, has not landed again, and the key has been let go, and canFly
        if (!mount.isOnGround() && mount.canFly() &&  airborne && !jumpOld && !mount.level.isClientSide && keyStrokeMovement.spacebar()) {
            mount.setFlying(true);
            return mod.add(0.0, 0.5, 0.0);
        }
        //only rejump if the key has been let go

        final double jumpStrength = mount.getAttributeValue(Attributes.JUMP_STRENGTH);

        //We want to jump,
        if (mount.isOnGround()){
            if (airborne){
                airborne = false;
            }
            if (mount.getKeyStrokeMovement().spacebar() && !airborne){
                airborne = true;
                mod = mount.jump(jumpStrength);
            }
            //If we're moving, either jumping or moving, scale by speedCoeficient, which is 1 in regular walk and 0.5 in slow walk
            mod.multiply(speedCoeficient, 1.0, speedCoeficient);
        }
        else {
            //If we're jumping, drop speed by a bit
            mod = mod.multiply(jumpStrength / 4, 1.0, jumpStrength / 4);
        }
        jumpOld = keyStrokeMovement.spacebar();
        return mod;
    }

}
