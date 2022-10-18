package witixin.mountables2.entity.movement;

import java.util.Objects;

public record MountTravel(Major major,
                          Minor minor,
                          MountMovement movement) {

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
