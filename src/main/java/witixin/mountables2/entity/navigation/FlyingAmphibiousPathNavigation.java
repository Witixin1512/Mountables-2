package witixin.mountables2.entity.navigation;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MountTravel;

import javax.annotation.Nullable;

/**
 * A navigation for {@link Mountable}s to be able to pathfind through Land, Air and Sea.
 */
public class FlyingAmphibiousPathNavigation extends PathNavigation {

    //public static final Navigator GROUND_NAVIGATOR = new Navigator() {};
    
    public FlyingAmphibiousPathNavigation(Mountable pMob, Level pLevel) {
        super(pMob, pLevel);
    }

    @Override
    protected PathFinder createPathFinder(int pMaxVisitedNodes) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
    }

    @Override
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override
    protected boolean canUpdatePath() {
        return false;
    }


    @Nullable
    private Navigator navigator() {
        if (this.mob instanceof Mountable mountable) {
            return switch (mountable.getMajor()) {
                case FLY -> null;
                case WALK -> null;
                case SWIM -> null;
            };
        }
        return null;
    }
}
