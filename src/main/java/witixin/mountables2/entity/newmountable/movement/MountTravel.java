package witixin.mountables2.entity.newmountable.movement;

import java.util.Objects;

public class MountTravel {
    private final Major major;
    private final Minor minor;
    private final MountMovement movement;

    public MountTravel(Major major, Minor minor, MountMovement movement) {
        this.major = major;
        this.minor = minor;
        this.movement = movement;
    }

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

    public Major major() {
        return major;
    }

    public Minor minor() {
        return minor;
    }

    public MountMovement movement() {
        return movement;
    }

    @Override
    public String toString() {
        return "MountTravel[" +
                "major=" + major + ", " +
                "minor=" + minor + ", " +
                "movement=" + movement + ']';
    }


    public enum Major {
        FLY, WALK, SWIM
    }

    public enum Minor {
        HOP, SLOW, FLOAT, SINK, NONE
    }
}
