package witixin.mountables2.entity.newmountable.movement;

import java.util.Objects;

public record MountTravel(witixin.mountables2.entity.newmountable.movement.MountTravel.Major major, witixin.mountables2.entity.newmountable.movement.MountTravel.Minor minor, MountMovement movement) {

    public static Minor from(String name) {
        try {
            return Minor.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Minor.NONE;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MountTravel other && other.major == major && other.minor == minor;
    }

    public enum Major {
        FLY, WALK, SWIM
    }

    public enum Minor {
        HOP, SLOW, FLOAT, SINK, NONE
    }
}
