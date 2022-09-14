package witixin.mountables2.entity.newmountable.movement;

import witixin.mountables2.entity.newmountable.Mountable;

public interface MountMovement {

    void travel(Mountable entity, MovementType type);

    enum MovementType {
        SPACEBAR
    }
}
