package witixin.mountables2.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.data.MountableInfo;
import witixin.mountables2.data.MountableManager;
import witixin.mountables2.entity.goal.MountableFloatGoal;
import witixin.mountables2.entity.goal.MountableFollowGoal;
import witixin.mountables2.entity.goal.MountableWanderGoal;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.client.ClientOpenScreenPacket;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Mountable extends TamableAnimal implements IAnimatable, PlayerRideableJumping {
    private static final double VERTICAL_SPEED_COEFICIENT = Math.cos(Math.toRadians(87));
    /*
    Credit to the Movement goes to LukeGrahamLaundry, maker of the Original Mountables Mod.
     */

    //Slime
    public float targetSquish = 1;
    public float squish = 1;
    public float oSquish;


    boolean wasOnGround = true;

    //Horse
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected boolean isJumping;
    protected float playerJumpPendingScale;
    private boolean allowStandSliding;
    protected int gallopSoundCounter;
    private boolean standing = true;
    boolean isFlying = false;
    int hopTimer = 10;
    boolean justAirJumped = false;
    boolean justLanded = false;

    boolean lockSwitch;

    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> FOLLOW_TYPE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> MODEL_POSITION = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> SQUISHY_SQUISHY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);


    public static final EntityDataAccessor<String> FREE_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> GROUND_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WATER_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> FLIGHT_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);

    public static final EntityDataAccessor<Boolean> CAN_FREE_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_FREE_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Float> ENTITY_WIDTH = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> ENTITY_HEIGHT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);

    private final AnimationFactory factory = new AnimationFactory(this);
    private MountableData mountableData;

    public Mountable(EntityType<Mountable> mountableEntityEntityType, Level level) {
        super(Mountables2Mod.MOUNTABLE_ENTITY.get(), level);
        this.maxUpStep = 1;
        if (!this.level.isClientSide){
            this.setFreeFly(getMountableData().ai_modes()[2]);
            this.setFreeSwim(getMountableData().ai_modes()[0]);
        }
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(3, new MountableFollowGoal(this));
        this.goalSelector.addGoal(3, new MountableWanderGoal(this));
        this.goalSelector.addGoal(1, new MountableFloatGoal(this));
    }


    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    public boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return super.isInvulnerableTo(pSource) || pSource.getMsgId().matches(DamageSource.FALL.getMsgId()) || pSource.getMsgId().matches(DamageSource.DROWN.getMsgId()) || pSource.isFire() || pSource.isExplosion();
    }

    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        this.dropMountableItem();
    }

    @javax.annotation.Nullable
    protected SoundEvent getAmbientSound() {
        return new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME) + ".idle"));
    }
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source){
        return new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME) + ".hurt"));

    }
    @Nullable
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState){
        this.playSound(new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME) + ".walk")), 0.8f, 0.15f);
    }

    @Override
    protected SoundEvent getSwimSound(){
        return new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME)+ ".swim"));
    }

    @Override
    protected SoundEvent getSwimSplashSound(){
        return new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME) + ".splash"));
    }

    @Override
    protected SoundEvent getDeathSound(){
        return new SoundEvent(Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME) + ".death"));
    }


    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    public void resetDefaultDataParameters(){
        setGroundMode(GROUND_MOVEMENT.WALK.name());
        setFreeMode(NON_RIDER.WALK.name());
        setWaterMode(WATER_MOVEMENT.FLOAT.name());
        setFlightMode(FLYING_MOVEMENT.NONE.name());
        this.onGround = true;
    }

    @Override
    public void tick(){
        super.tick();
        if (this.isNoGravity() && !this.isVehicle()) this.setNoGravity(false);
        if ((this.isControlledByLocalInstance() || this.isEffectiveAi()) && this.standCounter > 0 && ++this.standCounter > 20) {
            this.standCounter = 0;
            this.setStanding(false);
        }
        if (this.tailCounter > 0 && ++this.tailCounter > 8) {
            this.tailCounter = 0;
        }

        if (this.sprintCounter > 0) {
            ++this.sprintCounter;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }
        if (targetSquish == 1.0f){
            this.entityData.set(SQUISHY_SQUISHY, true);
        }
        if (squish == 0){
            this.entityData.set(SQUISHY_SQUISHY, false);
        }
        if (this.isStanding()) {
        } else {
            this.allowStandSliding = false;
        }

        this.squish += (this.targetSquish - this.squish) * 0.5F;
        this.oSquish = this.squish;
        if (this.onGround && !this.wasOnGround) {
            this.targetSquish = -0.5F;
        } else if (!this.onGround && this.wasOnGround) {
            this.targetSquish = 1.0F;
        }

        if (this.justLanded) {
            this.targetSquish = -0.5F;
            this.justLanded = false;
        } else if (this.justAirJumped) {
            this.targetSquish = 1.0F;
            this.justAirJumped = false;
        }

        this.wasOnGround = this.onGround;
        this.targetSquish *= 0.6F;
    }


    public void travel(Vec3 travelVec) {
        if (this.isAlive()) {
            //Flight or water controls; Water doesn't work yet
            if (this.isVehicle() && canMove3D()){
                LivingEntity rider = (LivingEntity) this.getPassengers().get(0);
                if ( !this.onGround &&  (this.isInWaterOrBubble() && this.entityData.get(WATER_MODE).matches(WATER_MOVEMENT.SWIM.name())) || (!this.isInWaterOrBubble() && this.entityData.get(FLIGHT_MODE).matches(FLYING_MOVEMENT.FLY.name())) ){
                    this.entityData.set(IS_FLYING, true);
                    }
                    if ( this.entityData.get(IS_FLYING) && (this.isInWaterOrBubble() && !this.entityData.get(WATER_MODE).matches(WATER_MOVEMENT.SWIM.name()) || (!this.isInWaterOrBubble() && !this.entityData.get(FLIGHT_MODE).matches(FLYING_MOVEMENT.FLY.name())))){
                        this.entityData.set(IS_FLYING, false);
                    }
                    this.setOnGround(!isFlying);
                    if (this.entityData.get(IS_FLYING)) {
                        this.setNoGravity(true);

                        this.setYRot(rider.getYRot());
                        this.yRotO = rider.getYRot();

                        double yComponent;
                        double moveForward = 0;

                        BlockState downState = this.level.getBlockState(this.blockPosition().below(2));
                        boolean downSolid = !(downState.isAir() || (this.canBeRiddenInWater(null) && downState.is(Blocks.WATER)));  // !isAir should be isSolid

                        if (rider.zza > 0) {
                            moveForward = this.getFlyingSpeed();
                            this.setXRot( -Mth.clamp(rider.getXRot(), -10, 10));
                            this.setRot(this.getYRot(), this.getXRot());
                            if (rider.getXRot() < 10 || rider.getXRot() > 10) {
                                yComponent = -(Math.toRadians(rider.getXRot()) * this.getFlyingSpeed());
                                if (!this.entityData.get(IS_FLYING) && yComponent > 0 && !this.onGround) this.entityData.set(IS_FLYING, true);
                                else if (this.entityData.get(IS_FLYING) && yComponent < 0 && downSolid)
                                    this.entityData.set(IS_FLYING, false);
                            }
                        } else if (rider.zza < 0) {
                            moveForward = -this.getFlyingSpeed();
                            this.setXRot(-Mth.clamp(rider.getXRot(), -10, 10));
                            this.setRot(this.getYRot(), this.getXRot());
                            if (rider.getXRot() < -10 || rider.getXRot() > 10) {
                                yComponent = (Math.toRadians(rider.getXRot()) * this.getFlyingSpeed());
                                if (!this.entityData.get(IS_FLYING) && yComponent > 0 && !this.onGround) this.entityData.set(IS_FLYING, true);
                                else if (this.entityData.get(IS_FLYING) && yComponent < 0 && downSolid)
                                    this.entityData.set(IS_FLYING, false);
                            }
                        }

                        if (this.isControlledByLocalInstance()){
                            this.flyingSpeed = this.getFlyingSpeed();
                            this.setSpeed(this.getFlyingSpeed());
                            super.travel(new Vec3(rider.xxa * this.getFlyingSpeed(), VERTICAL_SPEED_COEFICIENT * rider.getLookAngle().y, moveForward));
                        } else if (rider instanceof Player) {
                            this.setDeltaMovement(Vec3.ZERO);
                        }
                        return;
                    } else {
                        this.setNoGravity(false);
                    }
            }

            if (this.isVehicle() && this.canBeControlledByRider()) {
                LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
                this.setYRot(livingentity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                float f = livingentity.xxa * 0.5F;
                float f1 = livingentity.zza;
                if (f1 <= 0.0F) {
                    f1 *= 0.25F;
                    this.gallopSoundCounter = 0;
                }

                if (this.onGround && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
                    f = 0.0F;
                    f1 = 0.0F;
                }

                boolean allowJump = (this.onGround && !this.isJumping());
                if ((this.playerJumpPendingScale > 0.0F && allowJump)) {
                    this.justAirJumped = true;
                    double d0 = 0.65D * (double)this.playerJumpPendingScale * (double)this.getBlockJumpFactor();
                    double d1;
                    if (this.hasEffect(MobEffects.JUMP)) {
                        d1 = d0 + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                    } else {
                        d1 = d0;
                    }
                    if (this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name())) d1 *= 1.75;
                    if (this.canMove3D() && !this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name())) d1 = 0.5;

                    Vec3 vector3d = this.getDeltaMovement();
                    this.setDeltaMovement(vector3d.x, d1, vector3d.z);
                    this.setIsJumping(true);
                    this.hasImpulse = true;
                    ForgeHooks.onLivingJump(this);
                    if (f1 > 0.0F && this.getDeltaMovement().y <= 0.5) {
                        float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
                        float f3 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
                        float force = this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && canMove3D() ? 2 : 1;
                        super.travel(this.getDeltaMovement().add((double)(-0.4F * f2 * this.playerJumpPendingScale * force), 0.0D, (double)(0.4F * f3 * this.playerJumpPendingScale * force)));
                    }

                    this.playerJumpPendingScale = 0.0F;
                } else if (this.onGround && this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && (f != 0 || f1 != 0)){
                        makeHop(new Vec3(f1, 1, 1));
                }

                this.flyingSpeed = this.getSpeed() * 0.1F;
                if (this.isControlledByLocalInstance() && !this.getGroundMode().matches(GROUND_MOVEMENT.NONE.name())) {
                    float speed = this.isSlowOnLand() ? 0.05F : this.getFlyingSpeed();
                    this.setSpeed(speed);
                    //Move the groundmode check to the caclulate speed shenanigans
                    super.travel(new Vec3((double)f, travelVec.y, (double)f1));
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                if (this.onGround) {
                    this.playerJumpPendingScale = 0.0F;
                    this.setIsJumping(false);
                }

                this.calculateEntityAnimation(this, false);

                // flight logic
                if (livingentity.getXRot() < -25 && livingentity.zza > 0) this.entityData.set(IS_FLYING, true);
            } else {
                //AI MODE!
                float speed = this.isSlowOnLand() ? 0.05F : this.getFlyingSpeed();
                //This getFlyingSpeed = any speed I presume.
                this.setSpeed(speed);
                this.flyingSpeed = 0.02F;
                if (this.onGround && this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && (travelVec.x != 0 || travelVec.z != 0)){
                    makeHop(new Vec3(travelVec.z, 0.5,0.2F));
                }
                super.travel(travelVec);
            }
        }
    }

    private void makeHop(Vec3 movement) {
        hopTimer++;
        if (hopTimer >= 10) {
            hopTimer = 0;
            super.travel(doSlimeJump((float) movement.x, (float) movement.y, (float) movement.z));
        } else {
            setSpeed(0.0f);
            this.setDeltaMovement(Vec3.ZERO);
            super.travel(Vec3.ZERO);
        }
    }
    private boolean isSlowOnLand(){
        return this.getGroundMode().matches(GROUND_MOVEMENT.SLOW_WALK.name()) || this.getWaterMode().matches(WATER_MOVEMENT.SWIM.name());
    }
    protected boolean isAffectedByFluids() {
        boolean bool = entityData.get(WATER_MODE).matches(WATER_MOVEMENT.SWIM.name());
        return !bool;
    }

    @Override
    protected void defineSynchedData(){
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, MountableManager.get().get(0).uniqueName());
        this.entityData.define(IS_FLYING, false);
        this.entityData.define(FOLLOW_TYPE, "FOLLOW");
        this.entityData.define(EMISSIVE_TEXTURE, -1);
        this.entityData.define(MODEL_POSITION, 0);
        this.entityData.define(FREE_MODE, NON_RIDER.WALK.name());
        this.entityData.define(GROUND_MODE, GROUND_MOVEMENT.WALK.name());
        this.entityData.define(WATER_MODE, WATER_MOVEMENT.FLOAT.name());
        this.entityData.define(FLIGHT_MODE, FLYING_MOVEMENT.NONE.name());
        this.entityData.define(CAN_FREE_FLY, false);
        this.entityData.define(CAN_FREE_SWIM, false);
        this.entityData.define(SQUISHY_SQUISHY, false);
        this.entityData.define(ENTITY_WIDTH, 1.0f);
        this.entityData.define(ENTITY_HEIGHT, 1.0f);
    }

    private float getFlyingSpeed(){
        return 0.55f;
    }

    private Vec3 doSlimeJump(float f1, double yScale, double force) {
        double d1 = 0.8D * yScale;
        Vec3 vector3d = this.getDeltaMovement();
        this.setDeltaMovement(vector3d.x, d1, vector3d.z);
        this.setIsJumping(true);
        this.hasImpulse = true;
        if (f1 > 0.0F && vector3d.y <= 0.3) {
            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
            float f3 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
            return (this.getDeltaMovement().add((double)(-0.4F * f2 * force), 0.0D, (double)(0.4F * f3 * force)));
        }
        return new Vec3(0.0, 0.0, 0.0);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
        this.setFollowMode(tag.getString("follow_mode"));
        this.setAbsoluteEmissive(tag.getInt("emissive_texture"));
        this.setModelPosition(tag.getInt("model_position"));
        this.setFreeMode(tag.getString("free_mode"));
        this.setGroundMode(tag.getString("ground_mode"));
        this.setWaterMode(tag.getString("water_mode"));
        this.setFlightMode(tag.getString("flight_mode"));
        this.setFreeFly(tag.getBoolean("free_flight"));
        this.setFreeSwim(tag.getBoolean("free_swim"));
        this.entityData.set(IS_FLYING, tag.getBoolean("flying"));
        this.entityData.set(ENTITY_WIDTH, tag.getFloat("mountable_width"));
        this.entityData.set(ENTITY_HEIGHT, tag.getFloat("mountable_height"));
        this.lockSwitch = tag.getBoolean("lock_switch");
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putString("unique_mountable_name", getUniqueResourceLocation().getPath());
        tag.putString("follow_mode", getFollowMode());
        tag.putInt("emissive_texture", getEmissiveTextureIndex());
        tag.putInt("model_position", getModelPosition());
        tag.putString("free_mode", this.getFreeMode());
        tag.putString("ground_mode", this.getGroundMode());
        tag.putString("water_mode", this.getWaterMode());
        tag.putString("flight_mode", this.getFlightMode());
        tag.putBoolean("free_flight", this.canFreeFly());
        tag.putBoolean("free_swim", this.canFreeSwim());
        tag.putBoolean("flying", this.entityData.get(IS_FLYING));
        tag.putFloat("mountable_width", this.entityData.get(ENTITY_WIDTH));
        tag.putFloat("mountable_height", this.entityData.get(ENTITY_HEIGHT));
        tag.putBoolean("lock_switch", lockSwitch);
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }


    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void positionRider(Entity pPassenger) {
        super.positionRider(pPassenger);
        if (pPassenger instanceof Mob mob) {
            this.yBodyRot = mob.yBodyRot;
        }
        MountableData data = this.getMountableData();
        double sideX = data.position()[0];
        double sideZ = data.position()[2];
        double amplifier = Math.max(sideX, sideZ);
        double radius = Math.toRadians(yBodyRot) + Math.atan2(sideZ, sideX);//arctan to get the angle;
        double offsetX = Math.cos(radius) * amplifier;
        double offsetZ = Math.sin(radius) * amplifier;

        pPassenger.setPos(this.getX() + offsetX, this.getY() + data.position()[1], this.getZ() + offsetZ);
        if (pPassenger instanceof LivingEntity passenger) {
            passenger.yBodyRot = this.yBodyRot;
        }

    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof Player && ((Player) passenger).isLocalPlayer()) {
            passenger.yRotO = this.getYRot();
            passenger.setYRot(this.getYRot());
            passenger.setYHeadRot(this.getYRot());
        }
        if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.absMoveTo(this.lerpX, this.lerpY, this.lerpZ, (float) this.lerpYRot, (float) this.lerpXRot);
        }
    }

    public boolean isWalking(){
        return this.getGroundMode().matches(GROUND_MOVEMENT.SLOW_WALK.name()) || this.getGroundMode().matches(GROUND_MOVEMENT.WALK.name());
    }

    protected void doPlayerRide(Player pPlayer) {
        if (!this.level.isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }

    }
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (!pPlayer.level.isClientSide && pPlayer.isShiftKeyDown() && this.getOwnerUUID().equals(pPlayer.getUUID())){
            this.remove(RemovalReason.DISCARDED);
            this.dropMountableItem();
            return InteractionResult.SUCCESS;
        }
        if (itemstack.getItem().equals(Mountables2Mod.COMMAND_CHIP.get()) && !pPlayer.level.isClientSide && this.getOwnerUUID().equals(pPlayer.getUUID())){
            MountableInfo info = this.generateMountableInfo();
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) pPlayer), new ClientOpenScreenPacket(info));
            return InteractionResult.SUCCESS;
        }
        if (!this.isVehicle() && !pPlayer.level.isClientSide && pPlayer.getMainHandItem().getItem() != Mountables2Mod.COMMAND_CHIP.get()) {
            this.doPlayerRide(pPlayer);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(pPlayer, pHand);
    }

    public void dropMountableItem(){
        ItemStack mountable_item = new ItemStack(Mountables2Mod.MOUNTABLE.get());
        mountable_item.getOrCreateTag().putString("MOUNTABLE", this.entityData.get(UNIQUE_NAME));
        mountable_item.getTag().putString("FOLLOW_MODE", this.getFollowMode());
        mountable_item.getTag().putUUID("OWNER", this.getOwnerUUID());
        mountable_item.getTag().putInt("MODEL_POS", this.getModelPosition());
        mountable_item.getTag().putInt("TEX_POS", this.getEmissiveTextureIndex());
        mountable_item.getTag().putString("FLIGHT_MODE", this.getFlightMode());
        mountable_item.getTag().putString("GROUND_MODE", this.getGroundMode());
        mountable_item.getTag().putString("FREE_MODE", this.getFreeMode());
        mountable_item.getTag().putString("WATER_MODE", this.getWaterMode());
        if (this.isDeadOrDying()){
            mountable_item.getTag().putBoolean("dead", true);
        }
        if (this.lockSwitch){
            mountable_item.getTag().putBoolean("locked", true);
        }
        this.spawnAtLocation(mountable_item);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.getAnimatable() instanceof Mountable mountable){
            if (event.isMoving()) {
                if (mountable.isInWater()) event.getController().setAnimation(new AnimationBuilder().addAnimation("swim", true));
                if (mountable.onGround) event.getController().setAnimation(new AnimationBuilder().addAnimation("walk", true));
                if (!mountable.onGround && this.entityData.get(IS_FLYING)) event.getController().setAnimation(new AnimationBuilder().addAnimation("fly", true));
                    return PlayState.CONTINUE;
                    //TODO JUMP AND LAND
            }
            else {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", true));
            }
        }
        return PlayState.CONTINUE;
    }

    public MountableInfo generateMountableInfo(){
        Boolean[] moveRestrictions = this.getMountableData().ai_modes();
        return new MountableInfo(this.getUUID(), this.getFollowMode(), this.getFreeMode(), this.getGroundMode(), this.getWaterMode(), this.getFlightMode(), this.canFreeSwim(), this.canFreeFly(), moveRestrictions[0], moveRestrictions[1], moveRestrictions[2], lockSwitch);
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }


    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public boolean canMove3D(){
        return this.entityData.get(FLIGHT_MODE).matches(FLYING_MOVEMENT.FLY.name()) || this.entityData.get(WATER_MODE).matches(WATER_MOVEMENT.SWIM.name());
    }

    public ResourceLocation getUniqueResourceLocation(){
        return Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME));
    }
    public void setUniqueName(String s){
        this.entityData.set(UNIQUE_NAME, s);
    }

    public MountableData getMountableData(){
        if (this.mountableData == null) loadMountableData(Mountables2Mod.findData(getUniqueResourceLocation().getPath()));
        return this.mountableData;
    }

    public String getFollowMode(){
        return this.entityData.get(FOLLOW_TYPE);
    }
    public void setFollowMode(String s){
        if (Arrays.stream(FOLLOW_TYPES.values()).map(Enum::name).toList().contains(s)){
            this.entityData.set(FOLLOW_TYPE, s);
        }
        else {
            System.out.println("Invalid follow mode found! How did this happen!");
        }
    }

    private int getEmissiveTextureIndex(){
        return this.entityData.get(EMISSIVE_TEXTURE);
    }
    public String getEmissiveTexture(){
        final List<String> mountableData = getMountableData().emissiveTextures();
        final int textIndex = getEmissiveTextureIndex();
        if (textIndex == -1 || textIndex >= mountableData.size())return "transparent";
        return mountableData.get(getEmissiveTextureIndex());
    }
    //Should be server only
    public void setEmissiveTexture(int index){
        index += getEmissiveTextureIndex();
        if (index == -2) index = this.getMountableData().emissiveTextures().size() - 1;
        if (index >= this.getMountableData().emissiveTextures().size()) index = -1;
        this.entityData.set(EMISSIVE_TEXTURE, index);
    }
    public void setAbsoluteEmissive(int index){
        this.entityData.set(EMISSIVE_TEXTURE, index);
    }


    public void setModelPosition(int index){
        if (index < 0) index = MountableManager.get().size() - 1;
        if (index >= MountableManager.get().size()) index = 0;
        this.entityData.set(MODEL_POSITION, index);
    }
    public int getModelPosition(){
        return this.entityData.get(MODEL_POSITION);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (ENTITY_HEIGHT.equals(pKey)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(pKey);
    }


    public void loadMountableData(MountableData data){
        this.mountableData = data;
        this.setUniqueName(data.uniqueName());
        this.setCustomNameVisible(true);
        this.setCustomName(new TextComponent(data.displayName()));
        this.processAttributes(data.attributeMap());
        this.entityData.set(ENTITY_WIDTH, (float)data.width());
        this.entityData.set(ENTITY_HEIGHT, (float)data.height());
    }
    
    public void refreshDimensions(){
        EntityDimensions preEventDim = EntityDimensions.scalable(this.entityData.get(ENTITY_WIDTH), this.entityData.get(ENTITY_HEIGHT));
        this.eyeHeight = this.getEyeHeight(this.getPose(),preEventDim);
        net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = ForgeEventFactory.getEntitySizeForge(this, this.getPose(),preEventDim, eyeHeight);
        this.dimensions = sizeEvent.getNewSize();
        this.eyeHeight = sizeEvent.getNewEyeHeight();
        this.reapplyPosition();
        boolean flag = (double)this.dimensions.width <= 4.0D && (double)this.dimensions.height <= 4.0D;
        if (!this.level.isClientSide && !this.firstTick && !this.noPhysics && flag && (this.dimensions.width > preEventDim.width || this.dimensions.height > preEventDim.height)) {
            Vec3 vec3 = this.position().add(0.0D, (double)preEventDim.height / 2.0D, 0.0D);
            double d0 = (double)Math.max(0.0F, this.dimensions.width - preEventDim.width) + 1.0E-6D;
            double d1 = (double)Math.max(0.0F, this.dimensions.height - preEventDim.height) + 1.0E-6D;
            VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec3, d0, d1, d0));
            EntityDimensions finalEntitydimensions = this.dimensions;
            this.level.findFreePosition(this, voxelshape, vec3, (double)this.dimensions.width, (double)this.dimensions.height, (double)this.dimensions.width).ifPresent((p_185956_) -> {
                this.setPos(p_185956_.add(0.0D, (double)(-finalEntitydimensions.height) / 2.0D, 0.0D));
            });
        }
    }

    public void loadMountableData(int index){
        if (index < 0) index = MountableManager.get().size() - 1;
        if (index >= MountableManager.get().size()) index = 0;
        loadMountableData(MountableManager.get().get(index));
    }

    public void loadDefault(String s){
        loadMountableData(Mountables2Mod.findData(s));
    }

    public boolean canFreeFly(){return entityData.get(CAN_FREE_FLY);}
    public boolean canFreeSwim(){return entityData.get(CAN_FREE_SWIM);}
    public void setFreeFly(boolean value){this.entityData.set(CAN_FREE_FLY, value);}
    public void setFreeSwim(boolean value){this.entityData.set(CAN_FREE_SWIM, value);}

    public void setFreeMode(String s){
        if (enumContainsName(NON_RIDER.values(), s)){
            this.entityData.set(FREE_MODE, s);
        }
    }
    public String getFreeMode(){
        return this.entityData.get(FREE_MODE);
    }
    public void setGroundMode(String s){
        if (enumContainsName(GROUND_MOVEMENT.values(), s)){
            this.entityData.set(GROUND_MODE,s);
        }
    }
    public String getGroundMode(){
        return this.entityData.get(GROUND_MODE);
    }
    public void setWaterMode(String s){
        if (enumContainsName(WATER_MOVEMENT.values(), s)){
            this.entityData.set(WATER_MODE, s);
        }
    }
    public String getWaterMode(){return this.entityData.get(WATER_MODE);}
    public String getFlightMode(){return this.entityData.get(FLIGHT_MODE);}
    public void setFlightMode(String s){
        if (enumContainsName(FLYING_MOVEMENT.values(), s)){
            this.entityData.set(FLIGHT_MODE, s);
        }
    }

    @Override
    protected void jumpFromGround(){
        double d0 = (double)this.getJumpPower() + this.getJumpBoostPower();
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, 2, vec3.z);
        this.hasImpulse = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);
    }
    public boolean isJumping() {
        return this.isJumping;
    }
    public void setStanding(boolean p_110219_1_) {
        this.standing = p_110219_1_;
    }

    private void stand() {
        if (this.isControlledByLocalInstance() || this.isEffectiveAi()) {
            this.standCounter = 1;
            this.setStanding(true);
        }

    }
    public void setIsJumping(boolean p_110255_1_) {
        this.isJumping = p_110255_1_;
    }

    public boolean isStanding() {
        return this.standing;
    }
    public boolean isPushable() {
        return !this.isVehicle();
    }
    @Override
    public void onPlayerJump(int pJumpPower) {
        if (pJumpPower < 0) {
            pJumpPower = 0;
        } else {
            this.allowStandSliding = true;
            this.stand();
        }

        if (pJumpPower >= 90) {
            this.playerJumpPendingScale = 1.0F;
        } else {
            this.playerJumpPendingScale = 0.4F + 0.4F * (float)pJumpPower / 90.0F;
        }
    }

    private void processAttributes(Map<String, Double> dataAttributeMap){
        for (Map.Entry<String, Double> entry : dataAttributeMap.entrySet()){
            try {
                //String toCompare = FMLEnvironment.production ? Reference.SRG_ATTRIBUTES_MAP.get(entry.getKey()) : entry.getKey();
                Attribute attribute = (Attribute) ObfuscationReflectionHelper.findField(Attributes.class, Mountables2Mod.SRG_ATTRIBUTES_MAP.get(entry.getKey())).get(null);
                this.getAttribute(attribute).setBaseValue(entry.getValue());
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    public void setLockSwitch(boolean bool){
        this.lockSwitch = bool;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int pJumpPower) {
        this.allowStandSliding = true;
        this.stand();
        //this.playJumpSound();
    }

    @Override
    public void handleStopJump() {

    }

    public static enum FOLLOW_TYPES {
        FOLLOW,
        STAY,
        WANDER;
    }

    private static boolean enumContainsName(Enum[] objects, String s){
        return Arrays.stream(objects).anyMatch(value -> value.name().matches(s));
    }

    public static enum NON_RIDER {
        WALK, JUMP, SLOW_WALK;
    }
    public enum GROUND_MOVEMENT {
        NONE, WALK, SLOW_WALK, HOP;
    }
    public enum WATER_MOVEMENT {
        FLOAT, SWIM, SINK;
    }
    public enum FLYING_MOVEMENT {
        NONE, FLY, HOP
    }



}
