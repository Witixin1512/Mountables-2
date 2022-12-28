package witixin.mountables2.entity;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import witixin.mountables2.ClientReferences;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.data.MountableSerializer;
import witixin.mountables2.entity.goal.MountableFollowGoal;
import witixin.mountables2.entity.goal.MountableWanderGoal;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.entity.movement.MountTravel;
import witixin.mountables2.entity.movement.MovementRegistry;

import java.util.Map;

public class Mountable extends TamableAnimal implements GeoAnimatable {

    public static final EntityDataAccessor<Float> ENTITY_WIDTH = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> ENTITY_HEIGHT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_WALK = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MAJOR_MOVEMENT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Byte> FOLLOW_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> AIRBOURNE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_WALK = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Integer> HOP_TIMER = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);

    public static final byte FOLLOW = 0;
    public static final byte WANDER = 1;
    public static final byte STAY = 2;

    public static final String TRANSPARENT_EMISSIVE_TEXTURE = "transparent";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private MountableData mountableData;
    //Never set this value outside of setMajor()
    private MountTravel currentTravelMethod = MovementRegistry.INSTANCE.getMovement(MountTravel.Major.WALK, MountTravel.Minor.NONE);
    private boolean lockSwitch;
    private KeyStrokeMovement keyStrokeMovement = KeyStrokeMovement.NONE;

    private int hopTimer;

    public Mountable(EntityType<? extends Mountable> type, Level level) {
        super(Mountables2Mod.MOUNTABLE_ENTITY.get(), level);
        dimensions = getDimensions(Pose.STANDING); //pose
        setMajor(getMajor());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new MountableFollowGoal(this));
        this.goalSelector.addGoal(3, new MountableWanderGoal(this));
        //Is this handled in movement I assume then?
//        this.goalSelector.addGoal(1, new MountableFloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWaterOrBubble() && !currentTravelMethod.major().equals(MountTravel.Major.SWIM)) {
            setMajor(MountTravel.Major.SWIM);
        } else if (this.isOnGround() && !currentTravelMethod.major().equals(MountTravel.Major.WALK)) {
            setMajor(MountTravel.Major.WALK);
            if (this.isFlying()) setFlying(false);//walk when landing
        } else if (this.isFlying() && !currentTravelMethod.major().equals(MountTravel.Major.FLY)) {
            setMajor(MountTravel.Major.FLY);
        }
    }

    public MountTravel.Minor getMinorMovement(MountTravel.Major major) {
        return MountTravel.from(this.entityData.get(getEDAForMinor(major)));
    }

    public MountTravel.Major getMajor(){
        return MountTravel.Major.valueOf(this.entityData.get(MAJOR_MOVEMENT));
    }

    public void setMajor(MountTravel.Major major){
        setMajor(major, getMinorMovement(major));
    }

    public void setMajor(MountTravel.Major major, MountTravel.Minor minor){
        if (!this.level.isClientSide){
            this.entityData.set(MAJOR_MOVEMENT, major.name());
            setNoGravity(major.isNoGravity());
        }
        currentTravelMethod = MovementRegistry.INSTANCE.getMovement(major, minor);
    }

    public boolean isFlying() {
        return this.entityData.get(AIRBOURNE);
    }

    public void setFlying(boolean flag) {
        this.entityData.set(AIRBOURNE, flag);
    }

    public String getEmissiveTexture() {
        return this.entityData.get(EMISSIVE_TEXTURE);
    }

    public void setEmissiveTexture(String toSet) {
        this.entityData.set(EMISSIVE_TEXTURE, toSet);
    }

    public MountableData getMountableData() {
        if (this.mountableData == null) {
            this.mountableData = Mountables2Mod.findData(getUniqueResourceLocation().getPath(), this.getServer());
            loadMountableData(mountableData);
        }
        return this.mountableData;
    }

    public ResourceLocation getUniqueResourceLocation() {
        return Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME));
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

    /**
     * Reflection is here to support any of the vanilla attributes.
     * TODO maybe rethink this and only support the 5 regular ones.
     * @param dataAttributeMap
     */
    private void processAttributes(Map<String, Double> dataAttributeMap) {
        for (Map.Entry<String, Double> entry : dataAttributeMap.entrySet()) {
            try {
                Attribute attribute = (Attribute) ObfuscationReflectionHelper.findField(Attributes.class, Mountables2Mod.SRG_ATTRIBUTES_MAP.get(entry.getKey())).get(null);
                this.getAttribute(attribute).setBaseValue(entry.getValue());
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
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
                //TODO this.dropMountableItem();
                return InteractionResult.SUCCESS;
            }
            if (!this.isVehicle() && pPlayer.getMainHandItem().getItem() != Mountables2Mod.COMMAND_CHIP.get()) {
                if (!this.level.isClientSide) {
                    pPlayer.setYRot(this.getYRot());
                    pPlayer.setXRot(this.getXRot());
                    pPlayer.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            }
        } else if (pPlayer.getItemInHand(pHand).getItem().equals(Mountables2Mod.COMMAND_CHIP.get())) {
            if (pPlayer.getUUID().equals(getOwnerUUID())){
                ClientReferences.openCommandChipScreen(getId());
                return InteractionResult.sidedSuccess(true);
            } else {
                //TODO Check if sendSystemMEssage == sendmessage
                pPlayer.sendSystemMessage(Component.translatable("msg.mountables2.chip.owner"));
                return InteractionResult.FAIL;
            }

        }

        return super.mobInteract(pPlayer, pHand);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        Vec3 newvector = pTravelVector;

        if (isAlive()) {
            if (isVehicle() && getFirstPassenger() instanceof LivingEntity rider) {
                rotateBodyTo(rider);
                /********************handle rotation and moving forward************************/
                double deltaX = 0, deltaY = 0, deltaZ = 0;
                double rotation = this.getYRot() * (Mth.PI / 180F);

                double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);

                if (isFlying())
                    speed = this.getAttributeValue(Attributes.FLYING_SPEED);

                //Set slow speed modifier last
                if (getMinorMovement(currentTravelMethod.major()).equals(MountTravel.Minor.SLOW))
                    speed /= 2.0d;

                double sideWaysFactor = 0.60;//about two thirds of frontal movement
                double inverseX = keyStrokeMovement.forwards() && !keyStrokeMovement.backwards() ? 1 : -1;
                double inverseZ = keyStrokeMovement.left() && !keyStrokeMovement.right() ? 1 : -1;
                if (getKeyStrokeMovement().isFrontal())
                    deltaX = (keyStrokeMovement.isPurelyLateral() ? speed * sideWaysFactor : speed) * inverseX;
                if (getKeyStrokeMovement().isLateral())
                    deltaZ = (keyStrokeMovement.isPurelyFrontal() ? speed * sideWaysFactor : speed) * inverseZ;

                double sinXRot = Math.sin(-rotation) * deltaX;
                double cosXRot = Math.cos(rotation) * deltaX;
                double sinZRot = Math.sin(-rotation + Mth.PI / 2d) * deltaZ;
                double cosZRot = Math.cos(rotation - Mth.PI / 2d) * deltaZ;

                pTravelVector = new Vec3(sinXRot + sinZRot, deltaY, cosXRot + cosZRot);

                newvector = currentTravelMethod.movement().travel(this, pTravelVector);
            }
            else {

            }
        }
        this.setDeltaMovement(this.getDeltaMovement().add(newvector));
        super.travel(newvector);
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
        final Boolean[] AI_MODES = mountableData.aiModes();
        this.entityData.set(CAN_WALK, AI_MODES[MountableSerializer.WALK]);
        this.entityData.set(CAN_FLY, AI_MODES[MountableSerializer.FLY]);
        this.entityData.set(CAN_SWIM, AI_MODES[MountableSerializer.SWIM]);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (ENTITY_HEIGHT.equals(pKey)) {
            this.refreshDimensions();
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

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.entityData.get(ENTITY_WIDTH), this.entityData.get(ENTITY_HEIGHT));
    }

    /*
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));

    }

    private static final AnimationBuilder SWIM_ANIMATION = new AnimationBuilder().addAnimation("swim", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("walk", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder FLY_ANIMATION = new AnimationBuilder().addAnimation("fly", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.LOOP);



    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
            if (event.getAnimatable() instanceof Mountable mountable && event.isMoving()) {
                if (mountable.currentTravelMethod.major().equals(MountTravel.Major.SWIM)) {
                    event.getController().setAnimation(SWIM_ANIMATION);
                    return PlayState.CONTINUE;
                } else if (mountable.currentTravelMethod.major().equals(MountTravel.Major.WALK)) {
                    //TODO JUMP AND LAND
                    event.getController().setAnimation(WALK_ANIMATION);
                    return PlayState.CONTINUE;
                } else if (mountable.currentTravelMethod.major().equals(MountTravel.Major.FLY)) {
                    event.getController().setAnimation(FLY_ANIMATION);
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().setAnimation(IDLE_ANIMATION);
                    return PlayState.CONTINUE;
                }
            } else {
                event.getController().setAnimation(IDLE_ANIMATION);
                return PlayState.CONTINUE;
            }
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }


     */

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, "companion_block");
        this.entityData.define(EMISSIVE_TEXTURE, TRANSPARENT_EMISSIVE_TEXTURE);
        this.entityData.define(MINOR_MOVEMENT_FLY, MountTravel.Minor.NONE.name());
        this.entityData.define(MINOR_MOVEMENT_SWIM, MountTravel.Minor.NONE.name());
        this.entityData.define(MINOR_MOVEMENT_WALK, MountTravel.Minor.NONE.name());
        this.entityData.define(ENTITY_WIDTH, 1.0f);
        this.entityData.define(ENTITY_HEIGHT, 1.0f);
        this.entityData.define(FOLLOW_MODE, FOLLOW);
        this.entityData.define(AIRBOURNE, false);
        this.entityData.define(MAJOR_MOVEMENT, MountTravel.Major.WALK.name());
        this.entityData.define(CAN_FLY, false);
        this.entityData.define(CAN_WALK, false);
        this.entityData.define(CAN_SWIM, false);
        this.entityData.define(HOP_TIMER, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("unique_mountable_name", getUniqueResourceLocation().getPath());
        tag.putString("emissive_texture", getEmissiveTexture());

        tag.putBoolean("flying", this.entityData.get(AIRBOURNE));
        tag.putString("minor_fly", this.entityData.get(MINOR_MOVEMENT_FLY));
        tag.putString("minor_walk", this.entityData.get(MINOR_MOVEMENT_WALK));
        tag.putString("minor_swim", this.entityData.get(MINOR_MOVEMENT_SWIM));
        tag.putByte("follow_mode", this.entityData.get(FOLLOW_MODE));

        tag.putFloat("mountable_width", this.entityData.get(ENTITY_WIDTH));
        tag.putFloat("mountable_height", this.entityData.get(ENTITY_HEIGHT));
        tag.putBoolean("lock_switch", lockSwitch);
        tag.putString("movement_major", this.getMajor().name());

        tag.putBoolean("can_fly", this.canFly());
        tag.putBoolean("can_walk", this.canWalk());
        tag.putBoolean("can_swim", this.canSwim());

        tag.putInt("hop_timer", this.getHopTimer());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
        this.setEmissiveTexture(tag.getString("emissive_texture"));
        this.entityData.set(AIRBOURNE, tag.getBoolean("flying"));
        this.entityData.set(MINOR_MOVEMENT_FLY, tag.getString("minor_fly"));
        this.entityData.set(MINOR_MOVEMENT_WALK, tag.getString("minor_walk"));
        this.entityData.set(MINOR_MOVEMENT_SWIM, tag.getString("minor_swim"));
        this.entityData.set(FOLLOW_MODE, tag.getByte("follow_mode"));
        this.entityData.set(ENTITY_WIDTH, tag.getFloat("mountable_width"));
        this.entityData.set(ENTITY_HEIGHT, tag.getFloat("mountable_height"));
        this.lockSwitch = tag.getBoolean("lock_switch");
        this.setMajor(MountTravel.Major.valueOf(tag.getString("movement_major")));
        this.entityData.set(CAN_FLY, tag.getBoolean("can_fly"));
        this.entityData.set(CAN_WALK, tag.getBoolean("can_walk"));
        this.entityData.set(CAN_SWIM, tag.getBoolean("can_swim"));
        this.entityData.set(HOP_TIMER, tag.getInt("hop_timer"));
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

    public boolean getLockSwitch() {
        return lockSwitch;
    }

    public void setKeyStrokeMovement(KeyStrokeMovement keyStrokeMovement) {
        this.keyStrokeMovement = keyStrokeMovement;
    }

    public KeyStrokeMovement getKeyStrokeMovement() {
        return keyStrokeMovement;
    }

    public int getHopTimer() {
        return entityData.get(HOP_TIMER);
    }

    public void setHopTimer(int amount){
        this.entityData.set(HOP_TIMER, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return age;
    }
}
