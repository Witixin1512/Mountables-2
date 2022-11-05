package witixin.mountables2.entity.movement;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;

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
        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.NONE, new WalkNoneTravel()));

        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.SLOW, (mount, travelVector) ->
        {
            return travelVector;
        }));

        /*
        registerMovement(new MountTravel(MountTravel.Major.WALK, MountTravel.Minor.HOP, (mount, travelVector) -> {
            if (mount.isOnGround()){
                int timer = mount.getHopTimer();
                if (!mount.level.isClientSide){
                    mount.raiseHopTimer();
                }
                if (timer >= LAND_HOP_TIMER && (travelVector.x != 0.0 || travelVector.z != 0.0 || mount.getKeyStrokeMovement().spacebar())){
                    if (!mount.level.isClientSide){
                        mount.setHopTimer(LAND_HOP_TIMER);
                    }
                    travelVector = doSlimeJump(mount,  mount.getAttributeBaseValue(Attributes.JUMP_STRENGTH));
                }
                if (timer != 0){
                    travelVector = Vec3.ZERO;
                }
            }
            return travelVector;
        }));

         */

        registerMovement(new MountTravel(MountTravel.Major.FLY, MountTravel.Minor.NONE, (mount, travelVector) ->
        {
            double modifier = VERTICAL_COEFICIENT;
            if (mount.getControllingPassenger() instanceof Player player){
                modifier *= player.getLookAngle().y;
            }
            if (mount.getKeyStrokeMovement().spacebar()) {
                double up = travelVector.y < 0.2 ? 0.01f : -0.01;
                travelVector = travelVector.add(0, up + modifier , 0);
            }
            Entity rider = mount.getControllingPassenger();
            return travelVector;
        }));

        registerMovement(new MountTravel(MountTravel.Major.SWIM, MountTravel.Minor.NONE, (mount, travelVector) ->

        {
            return travelVector;
        }));
    }

    private Vec3 doSlimeJump(Mountable mountable, double yScale) {
        Vec3 vector3d = mountable.getDeltaMovement();
        float f2 = Mth.sin(mountable.getYRot() * ((float)Math.PI / 180F));
        float f3 = Mth.cos(mountable.getYRot() * ((float)Math.PI / 180F));
        return (vector3d.add((f2 * -yScale), yScale, (f3 * yScale)));
        }
    }
