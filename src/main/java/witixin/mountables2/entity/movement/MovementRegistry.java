package witixin.mountables2.entity.movement;

import net.minecraft.world.entity.player.Player;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.travel.FlyHopTravel;
import witixin.mountables2.entity.movement.travel.HopTravel;
import witixin.mountables2.entity.movement.travel.WalkTravel;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum MovementRegistry {

    INSTANCE;

    private static final double VERTICAL_COEFICIENT = Math.cos(Math.toRadians(87));

    public static final int LAND_HOP_TIMER = 20;

    private final Set<MountTravel> registry = new HashSet<>();

    public void registerMovement(MountTravel travel) {
        registry.add(travel);
    }

    public MountTravel getMovement(MountTravel.Major major, MountTravel.Minor minor) {
        Optional<MountTravel> movementType = registry.stream().filter(mountTravel -> mountTravel.major() == major && mountTravel.minor() == minor).findAny();
        if (movementType.isEmpty())
            throw new NullPointerException(String.format("no registered movement for %s & %s", major, minor));
        return movementType.get();
    }

    public List<MountTravel> getMajorMovement(MountTravel.Major major) {
        return registry.stream().filter(mountTravel -> mountTravel.major() == major).toList();
    }

    public void load() {
        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.NORMAL, new WalkTravel(1.0)));

        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.SLOW, new WalkTravel(0.5)));

        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.HOP, new HopTravel()));

        registerMovement(new MountTravel(MountTravel.Major.FLY, MountTravel.Minor.NORMAL, (mount, travelVector) ->
        {
            double modifier = VERTICAL_COEFICIENT;
            if (mount.getControllingPassenger() instanceof Player player) {
                modifier *= player.getLookAngle().y;
            }
            if (mount.getKeyStrokeMovement().spacebar()) {
                double up = travelVector.y < 0.2 ? 0.01f : -0.01;
                travelVector = travelVector.add(0, up + modifier, 0);
            }
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.FLY, MountTravel.Minor.HOP, new FlyHopTravel()));

        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.NORMAL, (mount, travelVector) ->

        {
            double modifier = VERTICAL_COEFICIENT;
            if (mount.isInWaterOrBubble()) {
                if (mount.getControllingPassenger() instanceof Player player && (Mountable.isVectorNotZero(travelVector) || mount.getKeyStrokeMovement().spacebar())) {
                    modifier *= player.getLookAngle().y;
                    return travelVector.add(0, modifier, 0);
                }
            }
            else {
                return travelVector.multiply(0.5, 1.0, 0.5);
            }
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.FLOAT, (mount, travelVec) -> {
          if (mount.isInWaterOrBubble()) {
              return travelVec.add(0, 0.1, 0);
          }
          else {
              return travelVec.multiply(0.5, 1.0, 0.5);
          }
        }));


        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.SINK, (mount, travelVec) -> {
            if (mount.isInWaterOrBubble()) {
                return travelVec.add(0, -0.15, 0);
            }
            else {
                return travelVec.multiply(0.5, 1.0, 0.5);
            }
        }));
    }
}
