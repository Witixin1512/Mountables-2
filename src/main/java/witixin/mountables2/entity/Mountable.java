package witixin.mountables2.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import witixin.mountables2.ClientReferences;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.data.MountableSerializer;
import witixin.mountables2.entity.goal.MountableFloatGoal;
import witixin.mountables2.entity.goal.MountableFollowGoal;
import witixin.mountables2.entity.goal.MountableWanderGoal;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.entity.movement.MountTravel;
import witixin.mountables2.entity.movement.MovementRegistry;

import java.util.Map;

public class Mountable extends TamableAnimal implements GeoEntity {

    public static final EntityDataAccessor<Float> ENTITY_WIDTH = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> ENTITY_HEIGHT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_WALK = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MAJOR_MOVEMENT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Byte> FOLLOW_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> AIRBORNE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_WALK = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> LOCK_SWITCH = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final byte FOLLOW = 0;
    public static final byte WANDER = 1;
    public static final byte STAY = 2;

    private static final double SIDEWAYS_FACTOR = 2d/3;

    public static final String TRANSPARENT_EMISSIVE_TEXTURE = "transparent";
    public static final String JUMP_ANIMATION_NAME = "jump";
    public static final String HOP_CONTROLLER = "hop_controller";

    public static final String LAND_CONTROLLER = "land_controller";
    public static final String LAND_ANIMATION_NAME = "land";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private MountableData mountableData;
    //Never set this value outside of setMajor()
    private MountTravel currentTravelMethod;
    private KeyStrokeMovement keyStrokeMovement = KeyStrokeMovement.NONE;

    private FlyingPathNavigation flyingNav;
    private WaterBoundPathNavigation waterNav;

    public Mountable(EntityType<? extends Mountable> type, Level level) {
        super(Mountables2Mod.MOUNTABLE_ENTITY.get(), level);
        this.dimensions = getDimensions(Pose.STANDING); //pose
        //Current major is unstable and will always give companion block for one tick.
        final MountTravel.Major currentMajor = this.getMajor();
        this.currentTravelMethod = MovementRegistry.INSTANCE.getMovement(currentMajor, getMinorMovement(currentMajor));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new MountableFollowGoal(this));
        this.goalSelector.addGoal(3, new MountableWanderGoal(this));
        this.goalSelector.addGoal(3, new MountableFloatGoal(this));

    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        this.flyingNav = new FlyingPathNavigation(this, level);
        this.waterNav = new WaterBoundPathNavigation(this, level);
        return super.createNavigation(pLevel);
    }

    @Override
    public PathNavigation getNavigation() {
        return switch (getMajor()) {
            case FLY -> this.flyingNav;
            case WALK -> this.navigation;
            case SWIM -> this.waterNav;
        };
    }

    /**
     * Uses getNavigation() to return the proper navigation rather than the field.
     */
    @Override
    public void serverAiStep() {
        ++this.noActionTime;
        this.level.getProfiler().push("sensing");
        this.getSensing().tick();
        this.level.getProfiler().pop();
        int i = this.level.getServer().getTickCount() + this.getId();
        if (i % 2 != 0 && this.tickCount > 1) {
            this.level.getProfiler().push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            this.level.getProfiler().pop();
            this.level.getProfiler().push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            this.level.getProfiler().pop();
        } else {
            this.level.getProfiler().push("targetSelector");
            this.targetSelector.tick();
            this.level.getProfiler().pop();
            this.level.getProfiler().push("goalSelector");
            this.goalSelector.tick();
            this.level.getProfiler().pop();
        }

        this.level.getProfiler().push("navigation");
        this.getNavigation().tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("mob tick");
        this.customServerAiStep();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("controls");
        this.level.getProfiler().push("move");
        this.moveControl.tick();
        this.level.getProfiler().popPush("look");
        this.lookControl.tick();
        this.level.getProfiler().popPush("jump");
        this.jumpControl.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().pop();
        this.sendDebugPackets();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWaterOrBubble() && !currentTravelMethod.major().equals(MountTravel.Major.SWIM) && canSwim()) {
            setMajor(MountTravel.Major.SWIM);
        } else if (this.isOnGround() && !currentTravelMethod.major().equals(MountTravel.Major.WALK) && canWalk() && !this.isInWaterOrBubble()) {
            setMajor(MountTravel.Major.WALK);
            if (this.isFlying()) {
                this.triggerAnim(LAND_CONTROLLER, LAND_ANIMATION_NAME);
                setFlying(false);//walk when landing
            }
        } else if (this.isFlying() && !currentTravelMethod.major().equals(MountTravel.Major.FLY) && canFly()) {
            setMajor(MountTravel.Major.FLY);
        }
    }

    public void ensureAbilities() {
        final boolean canWalk = this.canWalk();
        final boolean canSwim = this.canSwim();
        final boolean canFly = this.canFly();

        if (!canWalk && this.getMajor().equals(MountTravel.Major.WALK)) {
            setMajor(MountTravel.Major.SWIM);
        }
        if (!canSwim && this.getMajor().equals(MountTravel.Major.SWIM)) {
                setMajor(MountTravel.Major.FLY);
        }
        if (!canFly && this.getMajor().equals(MountTravel.Major.FLY)) {
            if (canWalk) {
                setMajor(MountTravel.Major.WALK);
            }
            else {
                setMajor(MountTravel.Major.SWIM);
            }
        }
    }

    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        return true;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return !this.canSwim();
    }

    public MountTravel.Minor getMinorMovement(MountTravel.Major major) {
        return MountTravel.from(this.entityData.get(getEDAForMinor(major)));
    }
    @Override
    public void die(DamageSource pCause) {
        if (!this.level.isClientSide) this.dropMountableItem(true);
    }

    public MountTravel.Major getMajor(){
        return MountTravel.Major.valueOf(this.entityData.get(MAJOR_MOVEMENT));
    }

    public void setMajor(MountTravel.Major major){
        this.setMajor(major, this.getMinorMovement(major));
    }

    public void setMajor(MountTravel.Major major, MountTravel.Minor minor){
        if (!this.level.isClientSide){
            this.entityData.set(MAJOR_MOVEMENT, major.name());
            this.entityData.set(this.getEDAForMinor(major), minor.name());
            this.setNoGravity(major == MountTravel.Major.FLY && minor == MountTravel.Minor.NORMAL);
        }
        currentTravelMethod = MovementRegistry.INSTANCE.getMovement(major, minor);
    }

    public boolean isFlying() {
        return this.entityData.get(AIRBORNE);
    }

    public void setFlying(boolean flag) {
        this.entityData.set(AIRBORNE, flag);
    }

    public String getEmissiveTexture() {
        return this.entityData.get(EMISSIVE_TEXTURE);
    }

    public void setEmissiveTexture(String toSet) {
        this.entityData.set(EMISSIVE_TEXTURE, toSet);
    }

    /**
     * Only callable on the server side
     * @return The MountableData of this Mountable type.
     */
    public MountableData getMountableData() {
        if (this.mountableData == null) {
            this.mountableData = Mountables2Mod.findData(this.getUniqueName(), this.getServer());
            this.loadMountableData(mountableData);
        }
        return this.mountableData;
    }

    public String getUniqueName() {
        return this.entityData.get(UNIQUE_NAME);
    }

    public void loadMountableData(MountableData data) {
        this.mountableData = data;
        this.setUniqueName(data.uniqueName());
        this.setCustomNameVisible(true);
        this.setCustomName(Component.literal(data.displayName()));
        this.processAttributes(data.attributeMap());
        this.updateMovementAbilities();
        this.entityData.set(ENTITY_WIDTH, (float) data.width());
        this.entityData.set(ENTITY_HEIGHT, (float) data.height());
    }

    public void setUniqueName(String s) {
        this.entityData.set(UNIQUE_NAME, s);
    }

    private void processAttributes(Map<String, Double> dataAttributeMap) {
        for (Map.Entry<String, Double> entry : dataAttributeMap.entrySet()) {
            try {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(entry.getKey()));
                this.getAttribute(attribute).setBaseValue(entry.getValue());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity entity ? entity : null;
    }

    public byte getFollowMode() {
        return this.entityData.get(FOLLOW_MODE);
    }

    public void setFollowMode(byte followMode) {
        this.entityData.set(FOLLOW_MODE, followMode);
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {

        if (!pPlayer.level.isClientSide) {
            if (pPlayer.isShiftKeyDown() && pPlayer.getUUID().equals(getOwnerUUID()) && pPlayer.getMainHandItem().getItem() != Mountables2Mod.COMMAND_CHIP.get()) {
                this.remove(RemovalReason.DISCARDED);
                if (!pPlayer.isCreative()) this.dropMountableItem(false);
                return InteractionResult.SUCCESS;
            }
            if (!this.isVehicle() && pPlayer.getMainHandItem().getItem() != Mountables2Mod.COMMAND_CHIP.get()) {
                pPlayer.setYRot(this.getYRot());
                pPlayer.setXRot(this.getXRot());
                pPlayer.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        } else if (pPlayer.getItemInHand(pHand).getItem().equals(Mountables2Mod.COMMAND_CHIP.get())) {
            if (pPlayer.getUUID().equals(getOwnerUUID())){
                ClientReferences.openCommandChipScreen(getId());
                return InteractionResult.sidedSuccess(true);
            } else {
                pPlayer.sendSystemMessage(Component.translatable("msg.mountables2.chip.owner"));
                return InteractionResult.PASS;
            }

        }

        return super.mobInteract(pPlayer, pHand);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        Vec3 newVector;
        if (isAlive()) {
            final boolean isBeingRidden = isVehicle() && getFirstPassenger() instanceof LivingEntity;
            if (isBeingRidden) {
                this.rotateBodyTo((LivingEntity) getFirstPassenger());
            }
            double deltaX = 0, deltaY = 0, deltaZ = 0;
            final double rotation = this.getYRot() * (Mth.PI / 180F);
            double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (isFlying()) speed = this.getAttributeValue(Attributes.FLYING_SPEED);

            if (isBeingRidden) {
                final int inverseX = keyStrokeMovement.forwards() && !keyStrokeMovement.backwards() ? 1 : -1;
                final int inverseZ = keyStrokeMovement.left() && !keyStrokeMovement.right() ? 1 : -1;
                if (getKeyStrokeMovement().isFrontal())
                    deltaX = (keyStrokeMovement.isPurelyLateral() ? speed * SIDEWAYS_FACTOR : speed) * inverseX;
                if (getKeyStrokeMovement().isLateral())
                    deltaZ = (keyStrokeMovement.isPurelyFrontal() ? speed * SIDEWAYS_FACTOR : speed) * inverseZ;

                final double sinXRot = Math.sin(-rotation) * deltaX;
                final double cosXRot = Math.cos(rotation) * deltaX;
                final double sinZRot = Math.sin(-rotation + Mth.PI / 2d) * deltaZ;
                final double cosZRot = Math.cos(rotation - Mth.PI / 2d) * deltaZ;

                pTravelVector = new Vec3(sinXRot + sinZRot, deltaY, cosXRot + cosZRot);
                newVector = currentTravelMethod.movement().travel(this, pTravelVector);
                this.rotateBodyTo((LivingEntity) getFirstPassenger());
                this.setDeltaMovement(this.getDeltaMovement().add(newVector));
                super.travel(newVector);
            }
            else {
                    //None riding movement always runs on the serverside
                    //Gotta figure out movement using the travelVector
                    newVector = currentTravelMethod.movement().travel(this, pTravelVector);
                    if (currentTravelMethod.minor() == MountTravel.Minor.HOP) this.setDeltaMovement(this.getDeltaMovement().add(newVector));
                    else if (currentTravelMethod.major() != MountTravel.Major.FLY) {
                        BlockPos targetPos = this.getNavigation().getTargetPos();
                        BlockPos pos = this.getOnPos();
                        if (targetPos != null && Mth.abs(targetPos.getY() - pos.getY()) < 2) {
                            double jumpStrength = this.getAttributeValue(Attributes.JUMP_STRENGTH);
                            if (targetPos.atY(0).distSqr(pos.atY(0)) < jumpStrength) newVector = newVector.add(this.jump(jumpStrength));
                        }
                    }
                super.travel(newVector);
            }
        }
    }

    public boolean canWalk() {
        return entityData.get(CAN_WALK);
    }

    public boolean canSwim() {
        return entityData.get(CAN_SWIM);
    }

    public boolean canFly() {
        return entityData.get(CAN_FLY);
    }

    public void updateMovementAbilities(){
        final Boolean[] AI_MODES = this.getMountableData().aiModes();
        this.entityData.set(CAN_WALK, AI_MODES[MountableSerializer.WALK]);
        this.entityData.set(CAN_FLY, AI_MODES[MountableSerializer.FLY]);
        this.entityData.set(CAN_SWIM, AI_MODES[MountableSerializer.SWIM]);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public void dropMountableItem(boolean death) {
        final ItemStack mountableStack = new ItemStack(Mountables2Mod.MOUNTABLE.get());
        final CompoundTag nbtData = new CompoundTag();
        nbtData.putString("MOUNTABLE", this.getMountableData().uniqueName());
        if (death) nbtData.putBoolean("MOUNTABLE_DEAD", true);
        if (this.getLockSwitch()) nbtData.putBoolean("MOUNTABLE_LOCKED", true);
        nbtData.putByte("FOLLOW_MODE", this.getFollowMode());
        mountableStack.setTag(nbtData);
        this.spawnAtLocation(mountableStack);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (ENTITY_HEIGHT.equals(pKey)) {
            this.refreshDimensions();
        }
        if (UNIQUE_NAME.equals(pKey)) {
            if (!level.isClientSide) this.updateMovementAbilities();
            this.ensureAbilities();
        }
        if (MAJOR_MOVEMENT.equals(pKey) || MINOR_MOVEMENT_SWIM.equals(pKey) || MINOR_MOVEMENT_WALK.equals(pKey) || MINOR_MOVEMENT_FLY.equals(pKey)) {
            currentTravelMethod = MovementRegistry.INSTANCE.getMovement(getMajor(), getMinorMovement(getMajor()));
        }
        super.onSyncedDataUpdated(pKey);
    }

    public void rotateBodyTo(LivingEntity rider) {
        this.setYRot(rider.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(rider.getXRot() * 0.5F);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.yBodyRot;
    }

    public Vec3 jump(double jumpStrength) {
        Vec3 vec = new Vec3(0, jumpStrength, 0);
        this.triggerAnim(Mountable.HOP_CONTROLLER, Mountable.JUMP_ANIMATION_NAME);
        this.setOnGround(false);
        return vec;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.entityData.get(ENTITY_WIDTH), this.entityData.get(ENTITY_HEIGHT));
    }


    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HOP_ANIMATION = RawAnimation.begin().thenPlay(JUMP_ANIMATION_NAME);
    private static final RawAnimation LAND_ANIMATION = RawAnimation.begin().thenPlay(LAND_ANIMATION_NAME);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "base_controller", 10, state -> {
            if (state.isMoving()) {
                final Mountable mount = state.getAnimatable();
                final boolean onGround = mount.isOnGround();
                if (mount.getMajor() == MountTravel.Major.FLY && mount.getMinorMovement(MountTravel.Major.FLY) == MountTravel.Minor.NORMAL)
                    return state.setAndContinue(FLY_ANIMATION);
                if (mount.getMajor() == MountTravel.Major.SWIM) return state.setAndContinue(SWIM_ANIMATION);
                if (mount.getMajor() == MountTravel.Major.WALK && mount.getMinorMovement(MountTravel.Major.WALK) != MountTravel.Minor.HOP && onGround)
                    return state.setAndContinue(WALK_ANIMATION);
            }
            return state.setAndContinue(IDLE_ANIMATION);
        }));
        controllers.add(new AnimationController<>(this, HOP_CONTROLLER, 1, state -> PlayState.STOP)
                .triggerableAnim(JUMP_ANIMATION_NAME, HOP_ANIMATION));
        controllers.add(new AnimationController<>(this, LAND_CONTROLLER, 1, state -> PlayState.STOP)
                .triggerableAnim(LAND_ANIMATION_NAME, LAND_ANIMATION));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, "companion_block");
        this.entityData.define(EMISSIVE_TEXTURE, TRANSPARENT_EMISSIVE_TEXTURE);
        this.entityData.define(MINOR_MOVEMENT_FLY, MountTravel.Minor.NORMAL.name());
        this.entityData.define(MINOR_MOVEMENT_SWIM, MountTravel.Minor.NORMAL.name());
        this.entityData.define(MINOR_MOVEMENT_WALK, MountTravel.Minor.NORMAL.name());
        this.entityData.define(ENTITY_WIDTH, 1.0f);
        this.entityData.define(ENTITY_HEIGHT, 1.0f);
        this.entityData.define(FOLLOW_MODE, FOLLOW);
        this.entityData.define(AIRBORNE, false);
        this.entityData.define(MAJOR_MOVEMENT, MountTravel.Major.WALK.name());
        this.entityData.define(CAN_FLY, false);
        this.entityData.define(CAN_WALK, false);
        this.entityData.define(CAN_SWIM, false);
        this.entityData.define(LOCK_SWITCH, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("unique_mountable_name", this.getUniqueName());
        tag.putString("emissive_texture", this.getEmissiveTexture());

        tag.putBoolean("flying", this.entityData.get(AIRBORNE));
        tag.putString("minor_fly", this.entityData.get(MINOR_MOVEMENT_FLY));
        tag.putString("minor_walk", this.entityData.get(MINOR_MOVEMENT_WALK));
        tag.putString("minor_swim", this.entityData.get(MINOR_MOVEMENT_SWIM));
        tag.putByte("follow_mode", this.entityData.get(FOLLOW_MODE));

        tag.putFloat("mountable_width", this.entityData.get(ENTITY_WIDTH));
        tag.putFloat("mountable_height", this.entityData.get(ENTITY_HEIGHT));
        tag.putBoolean("lock_switch", this.getLockSwitch());
        tag.putString("movement_major", this.getMajor().name());

        tag.putBoolean("can_fly", this.canFly());
        tag.putBoolean("can_walk", this.canWalk());
        tag.putBoolean("can_swim", this.canSwim());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
        this.setEmissiveTexture(tag.getString("emissive_texture"));
        this.entityData.set(AIRBORNE, tag.getBoolean("flying"));
        this.entityData.set(MINOR_MOVEMENT_FLY, tag.getString("minor_fly"));
        this.entityData.set(MINOR_MOVEMENT_WALK, tag.getString("minor_walk"));
        this.entityData.set(MINOR_MOVEMENT_SWIM, tag.getString("minor_swim"));
        this.entityData.set(FOLLOW_MODE, tag.getByte("follow_mode"));
        this.entityData.set(ENTITY_WIDTH, tag.getFloat("mountable_width"));
        this.entityData.set(ENTITY_HEIGHT, tag.getFloat("mountable_height"));
        this.setLockSwitch(tag.getBoolean("lock_switch"));
        this.setMajor(MountTravel.Major.valueOf(tag.getString("movement_major")));
        this.entityData.set(CAN_FLY, tag.getBoolean("can_fly"));
        this.entityData.set(CAN_WALK, tag.getBoolean("can_walk"));
        this.entityData.set(CAN_SWIM, tag.getBoolean("can_swim"));
    }


    //called from interaction screen with entity
    public void setMinorMovement(MountTravel.Major major, MountTravel.Minor minor) {
        this.setMajor(major, minor);
    }

    private EntityDataAccessor<String> getEDAForMinor(MountTravel.Major major) {
        return switch (major) {
            case FLY -> MINOR_MOVEMENT_FLY;
            case SWIM -> MINOR_MOVEMENT_SWIM;
            case WALK -> MINOR_MOVEMENT_WALK;
        };
    }

    public void setKeyStrokeMovement(KeyStrokeMovement keyStrokeMovement) {
        this.keyStrokeMovement = keyStrokeMovement;
    }

    public KeyStrokeMovement getKeyStrokeMovement() {
        return keyStrokeMovement;
    }

    public static boolean isVectorNotZero(Vec3 vec3) {
        return !vec3.add(Vec3.ZERO).equals(Vec3.ZERO);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean getLockSwitch() {
        return this.entityData.get(LOCK_SWITCH);
    }

    public void setLockSwitch(boolean bool) {
        this.entityData.set(LOCK_SWITCH, bool);
    }

}
