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
        Optional<MountTravel> movementType = registry.stream().filter(mountTravel -> mountTravel.getMajor() == major && mountTravel.getMinor() == minor).findAny();
        if (movementType == null || movementType.isEmpty() || !movementType.isPresent())
            throw new NullPointerException(String.format("no registered movement for %s & %s", major, minor));
        return movementType.get();
    }

    public List<MountTravel> getMajorMovement(MountTravel.Major major) {
        return registry.stream().filter(mountTravel -> mountTravel.getMajor() == major).toList();
    }

    public void load() {
        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.NONE, (mount, travelVector, type) -> {
            if (type == MountMovement.MovementType.REGULAR) {
                if (mount.isAlive() && mount.isVehicle() && mount.canBeControlledByRider()) {
                }
            }
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.SLOW, (mount, travelVector, type) ->
            //Why are those not direct lambdas?
        {

            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.FLY, MountTravel.Minor.NONE, (mount, travelVector, type) ->

        {

            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.NONE, (mount, travelVector, type) ->

        {
            return travelVector;
        }));
    }
}
