package witixin.mountables2.entity.movement;

import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;

public interface MountMovement {


    /**
     * Called on both sides
     * @param entity
     * @param travelVector
     * @return
     */
    Vec3 travel(Mountable entity, Vec3 travelVector);


}
