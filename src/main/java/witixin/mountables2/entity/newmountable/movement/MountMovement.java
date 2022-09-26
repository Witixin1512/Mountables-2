package witixin.mountables2.entity.newmountable.movement;

import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.newmountable.Mountable;

public interface MountMovement {

    Vec3 travel(Mountable entity, Vec3 travelVector);

}
