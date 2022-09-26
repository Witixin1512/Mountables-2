package witixin.mountables2.entity.newmountable.movement;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum MovementRegistry {

    INSTANCE;

    private final Set<MountTravel> registry = new HashSet<>();

    public void registerMovement(MountTravel travel) {
        registry.add(travel);
    }

    public MountTravel getMovement(MountTravel.Major major, MountTravel.Minor minor) {
        Optional<MountTravel> movementType = registry.stream().filter(mountTravel -> mountTravel.major() == major && mountTravel.minor() == minor).findAny();
        if (movementType == null || movementType.isEmpty() || !movementType.isPresent())
            throw new NullPointerException(String.format("no registered movement for %s & %s", major, minor));
        return movementType.get();
    }

    public List<MountTravel> getMajorMovement(MountTravel.Major major) {
        return registry.stream().filter(mountTravel -> mountTravel.major() == major).toList();
    }

    public void load() {
        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.NONE, (mount, travelVector) -> {
            if (mount.canFly()) {
                if (mount.getKeyStrokeMovement().jump()) {
                    mount.hasImpulse = true;
                    travelVector = travelVector.add(0, 0.1f, 0);
                    mount.setFlying(true);
                }
            }
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.SLOW, (mount, travelVector) ->
        {
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.FLY, MountTravel.Minor.NONE, (mount, travelVector) ->
        {
            mount.setNoGravity(true);
            if (mount.getKeyStrokeMovement().down())
                travelVector = travelVector.add(0, -0.2, 0);
            if (mount.getKeyStrokeMovement().jump()) {
                double up = travelVector.y < 0.2 ? 0.01f : 0.0;
                travelVector = travelVector.add(0, up, 0);
            }
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.NONE, (mount, travelVector) ->
        {
            return travelVector;
        }));
    }
}
