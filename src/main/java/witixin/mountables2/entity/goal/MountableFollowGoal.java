package witixin.mountables2.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.MountTravel;

import java.util.EnumSet;

public class MountableFollowGoal extends Goal {
        private final Mountable mountable;
        private LivingEntity owner;
        private int timeToRecalcPath;
        private float oldWaterCost;

        public MountableFollowGoal(Mountable mountable) {
            this.mountable = mountable;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = this.mountable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.mountable.isOrderedToSit()) {
                return false;
            } else if (this.mountable.distanceToSqr(livingentity) < this.getFollowRange() * 5 || !(this.mountable.getRandom().nextInt(10) < 3)) {
                return false;
            }
            else {
                this.owner = livingentity;
                return mountable.getControllingPassenger() == null && mountable.getFollowMode() == Mountable.FOLLOW;
            }
        }

        public double getFollowRange() {
           return this.mountable.getAttributeBaseValue(Attributes.FOLLOW_RANGE);
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            if (this.mountable.getNavigation().isDone()) {
                return false;
            } else if (this.mountable.isOrderedToSit()) {
                return false;
            } else {
                return !(this.mountable.distanceToSqr(this.owner) <= (this.mountable.getBbWidth() * this.mountable.getBbWidth()));
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.mountable.getPathfindingMalus(BlockPathTypes.WATER);
            this.mountable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.owner = null;
            this.mountable.getNavigation().stop();
            this.mountable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            this.mountable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.mountable.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (!this.mountable.isLeashed() && !this.mountable.isPassenger()) {
                    if (this.mountable.distanceToSqr(this.owner) >= this.getFollowRange() * this.getFollowRange()) {
                        this.teleportToOwner();
                    } else {
                        this.mountable.getNavigation().moveTo(this.owner, 1.0);
                    }

                }
            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = this.owner.blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean maybeTeleportTo(int pX, int pY, int pZ) {
            if (Math.abs((double)pX - this.owner.getX()) < 2.0D && Math.abs((double)pZ - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
                return false;
            } else {
                this.mountable.moveTo((double)pX + 0.5D, (double)pY, (double)pZ + 0.5D, this.mountable.getYRot(), this.mountable.getXRot());
                this.mountable.getNavigation().stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pPos) {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.mountable.level, pPos.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE && mountable.getMajor() == MountTravel.Major.WALK) {
                return false;
            }
            else if (blockpathtypes != BlockPathTypes.WATER && mountable.getMajor() == MountTravel.Major.SWIM) {
                return false;
            }
            else {
                //Flying
                BlockState blockstate = this.mountable.level.getBlockState(pPos.below());
                if (!(mountable.getMajor() == MountTravel.Major.FLY) && blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pPos.subtract(this.mountable.blockPosition());
                    return this.mountable.level.noCollision(this.mountable, this.mountable.getBoundingBox().move(blockpos));
                }
            }
        }

        private int randomIntInclusive(int pMin, int pMax) {
            return this.mountable.getRandom().nextInt(pMax - pMin + 1) + pMin;
        }
    }
