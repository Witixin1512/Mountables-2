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
                "minor=" + minor + "]";
    }


    public enum Major {
        FLY(true), WALK(false), SWIM(false);

        private final boolean noGravity;

        Major(boolean noGravity){
            this.noGravity = noGravity;
        }

        public boolean isNoGravity() {
            return noGravity;
        }
    }

    /**
     * Minor movement types are modifiers that are applied to the various {@link Major} movement types.
     * {@link Minor.NONE} implies that it should work normally, whereas the other types are intuitive.
     */

    public enum Minor {
        HOP, SLOW, FLOAT, SINK, NONE
    }
}