package witixin.mountables2.entity.newmountable.movement;

import java.util.Objects;

public class MountTravel {
    private final Major major;
    private final Minor minor;
    private final MountMovement movement;

    public MountTravel(Major major, Minor minor, MountMovement movement) {
        this.major = major;
        this.movement = movement;
        this.minor = minor;
    }

    public static Minor from(String name) {
        try {
            return Minor.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Minor.NONE;
        }
    }

    public Major getMajor() {
        return major;
    }

    public Minor getMinor() {
        return minor;
    }

    public MountMovement getMovement() {
        return movement;
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
