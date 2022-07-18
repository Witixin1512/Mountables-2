package witixin.mountables2.data;

import java.util.UUID;

public record MountableInfo(UUID uuid, String followMode, String freeMode, String groundMode, String waterMode, String flightMode, boolean waterState, boolean flightState, boolean canSwim, boolean canWalk, boolean canFly) {
}
