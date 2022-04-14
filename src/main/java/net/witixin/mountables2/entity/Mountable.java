package net.witixin.mountables2.entity;

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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.data.MountableData;
import net.witixin.mountables2.data.MountableInfo;
import net.witixin.mountables2.entity.goal.MountableFloatGoal;
import net.witixin.mountables2.entity.goal.MountableFollowGoal;
import net.witixin.mountables2.entity.goal.MountableWanderGoal;
import net.witixin.mountables2.network.client.ClientOpenScreenPacket;
import net.witixin.mountables2.network.PacketHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Mountable extends TamableAnimal implements IAnimatable, PlayerRideableJumping {

    public static final String DEFAULT_NAME = "companion_block";

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



    private AnimationFactory factory = new AnimationFactory(this);
    private MountableData mountableData;

    public Mountable(EntityType<Mountable> mountableEntityEntityType, Level level) {
        super(Reference.MOUNTABLE_ENTITY.get(), level);
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
        return new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME) + ".idle"));
    }
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source){
        return new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME) + ".hurt"));

    }
    @Nullable
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState){
        this.playSound(new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME) + ".walk")), 0.8f, 0.15f);
    }

    @Override
    protected SoundEvent getSwimSound(){
        return new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME)+ ".swim"));
    }

    @Override
    protected SoundEvent getSwimSplashSound(){
        return new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME) + ".splash"));
    }

    @Override
    protected SoundEvent getDeathSound(){
        return new SoundEvent(Reference.rl(this.entityData.get(UNIQUE_NAME) + ".death"));
    }


    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
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

    //TODO Override sounds.


    public void travel(Vec3 travelVec) {
        if (this.isAlive()) {
            if (this.isVehicle() && (this.canFreeFly() || this.canBeRiddenInWater(null)) && this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name())){
                LivingEntity rider = (LivingEntity) this.getPassengers().get(0);

                if (this.canBeRiddenInWater(rider) && this.isInWaterOrBubble()){
                    this.entityData.set(IS_FLYING, true);
                }
                if (this.canBeRiddenInWater(rider) && !this.isInWaterOrBubble() && this.isFlying && !this.canFreeFly()){
                    this.entityData.set(IS_FLYING, false);
                }
                this.setOnGround(!isFlying);

                if (isFlying) {
                    this.setNoGravity(true);

                    this.setYRot(rider.getYRot());
                    this.yRotO = rider.getYRot();

                    double yComponent = 0;
                    double moveForward = 0;

                    BlockState downState = this.level.getBlockState(this.blockPosition().below(2));
                    boolean downSolid = !(downState.isAir() || (this.canBeRiddenInWater(null) && downState.is(Blocks.WATER)));  // !isAir should be isSolid

                    if (rider.zza > 0) {
                        moveForward = this.getFlyingSpeed();
                        this.setXRot( -Mth.clamp(rider.getXRot(), -10, 10));
                        this.setRot(this.getYRot(), this.getXRot());
                        if (rider.getXRot() < -10 || rider.getXRot() > 10) {
                            yComponent = -(Math.toRadians(rider.getXRot()) * this.getFlyingSpeed());
                            if (!isFlying && yComponent > 0) this.entityData.set(IS_FLYING, true);
                            else if (isFlying && yComponent < 0 && downSolid)
                                this.entityData.set(IS_FLYING, false);
                        }
                    } else if (rider.zza < 0) {
                        moveForward = -this.getFlyingSpeed();
                        this.setXRot(-Mth.clamp(rider.getXRot(), -10, 10));
                        this.setRot(this.getYRot(), this.getXRot());
                        if (rider.getXRot() < -10 || rider.getXRot() > 10) {
                            yComponent = (Math.toRadians(rider.getXRot()) * this.getFlyingSpeed());
                            if (!isFlying && yComponent > 0) this.entityData.set(IS_FLYING, true);
                            else if (isFlying && yComponent < 0 && downSolid)
                                this.entityData.set(IS_FLYING, false);
                        }
                    }

                    if (this.isControlledByLocalInstance()){
                        this.flyingSpeed = (float) this.getFlyingSpeed();
                        // this.setSpeed((float) this.getFlyingSpeed());
                        super.travel(new Vec3(rider.xxa * this.getFlyingSpeed() * 0.5F, yComponent, moveForward));
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

                boolean allowJump = (this.onGround && !this.isJumping()) || (this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && canFreeFly()) ;
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
                    if (this.canFreeFly() && !this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name())) d1 = 0.5;

                    Vec3 vector3d = this.getDeltaMovement();
                    this.setDeltaMovement(vector3d.x, d1, vector3d.z);
                    this.setIsJumping(true);
                    this.hasImpulse = true;
                    ForgeHooks.onLivingJump(this);
                    if (f1 > 0.0F) {
                        float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
                        float f3 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
                        float force = this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && canFreeFly() ? 2 : 1;
                        this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f2 * this.playerJumpPendingScale * force), 0.0D, (double)(0.4F * f3 * this.playerJumpPendingScale * force)));
                    }

                    this.playerJumpPendingScale = 0.0F;
                    if (canFreeFly()) this.entityData.set(IS_FLYING, true);
                } else if (this.onGround && this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && (f != 0 || f1 != 0)){
                    if (hopTimer >= 3){
                        doSlimeJump((float) f1, 1, 1);
                        hopTimer = 0;
                    }
                    hopTimer++;
                }

                this.flyingSpeed = this.getSpeed() * 0.1F;
                if (this.isControlledByLocalInstance()) {
                    float speed = this.isSlowOnLand() ? 0.05F : this.getFlyingSpeed();
                    this.setSpeed(speed);
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
                float speed = this.isSlowOnLand() ? 0.05F : this.getFlyingSpeed();
                this.setSpeed(speed);
                this.flyingSpeed = 0.02F;
                if (this.onGround && this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && (travelVec.x != 0 || travelVec.z != 0)){
                    if (hopTimer >= 5){
                        doSlimeJump((float) travelVec.z, 0.5,0.2F);
                        hopTimer = 0;
                    }
                    hopTimer++;
                }
                super.travel(travelVec);
            }


        }
    }

    private boolean isSlowOnLand(){
        return this.getGroundMode().matches(GROUND_MOVEMENT.SLOW_WALK.name()) || this.getWaterMode().matches(WATER_MOVEMENT.SWIM.name());
    }

    @Override
    protected void defineSynchedData(){
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, DEFAULT_NAME);
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
    }

    private float getFlyingSpeed(){
        return Math.max(0.25F, 0.025F);
    }

    private void doSlimeJump(float f1, double yScale, double force) {
        double d1 = 0.8D * yScale;
        Vec3 vector3d = this.getDeltaMovement();
        this.setDeltaMovement(vector3d.x, d1, vector3d.z);
        this.setIsJumping(true);
        this.hasImpulse = true;
        if (f1 > 0.0F) {
            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
            float f3 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
            this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f2 * force), 0.0D, (double)(0.4F * f3 * force)));
        }

    }


    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
        this.setFollowMode(tag.getString("follow_mode"));
        this.setEmissiveTexture(tag.getInt("emmisive_texture"));
        this.setModelPosition(tag.getInt("model_position"));
        this.setFreeMode(tag.getString("free_mode"));
        this.setGroundMode(tag.getString("ground_mode"));
        this.setWaterMode(tag.getString("water_mode"));
        this.setFlightMode(tag.getString("flight_mode"));
        this.setFreeFly(tag.getBoolean("free_flight"));
        this.setFreeSwim(tag.getBoolean("free_swim"));
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
        if (pPassenger instanceof Mob) {
            Mob mob = (Mob)pPassenger;
            this.yBodyRot = mob.yBodyRot;
        }
            MountableData data = this.getMountableData();
            pPassenger.setPos(this.getX() + data.position()[0], this.getY() + data.position()[1], this.getZ() + data.position()[2]);
            if (pPassenger instanceof LivingEntity) {
                ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
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
    public boolean can3dMove(){
        return false;
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
        if (itemstack.getItem().equals(Reference.COMMAND_CHIP.get()) && !pPlayer.level.isClientSide && this.getOwnerUUID().equals(pPlayer.getUUID())){
            Boolean[] moveRestrictions = this.getMountableData().ai_modes();
            MountableInfo info = new MountableInfo(this.getUUID(), this.getFollowMode(), this.getFreeMode(), this.getGroundMode(), this.getWaterMode(), this.getFlightMode(), this.canFreeSwim(), this.canFreeFly(), moveRestrictions[0], moveRestrictions[1], moveRestrictions[2]);
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) pPlayer), new ClientOpenScreenPacket(info));
            return InteractionResult.SUCCESS;
        }
        if (!this.isVehicle() && !pPlayer.level.isClientSide && pPlayer.getMainHandItem().getItem() != Reference.COMMAND_CHIP.get()) {
            this.doPlayerRide(pPlayer);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(pPlayer, pHand);
    }

    public void dropMountableItem(){
        ItemStack mountable_item = new ItemStack(Reference.MOUNTABLE.get());
        mountable_item.getOrCreateTag().putString("MOUNTABLE", this.getUniqueResourceLocation().getPath());
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



    public ResourceLocation getUniqueResourceLocation(){
        if (this.entityData.get(UNIQUE_NAME) == null ) throw new RuntimeException("Unique Name cannot be null!");
        return Reference.rl(this.entityData.get(UNIQUE_NAME));
    }
    public void setUniqueName(String s){
        this.entityData.set(UNIQUE_NAME, s);
    }

    public MountableData getMountableData(){
        if (this.mountableData == null) loadMountableData(Reference.findData(getUniqueResourceLocation().getPath()));
        return this.mountableData;
    }

    public String getFollowMode(){
        return this.entityData.get(FOLLOW_TYPE);
    }
    public void setFollowMode(String s){
        if (Arrays.asList(FOLLOW_TYPES.values()).stream().map(Enum::name).collect(Collectors.toList()).contains(s)){
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
        if (getEmissiveTextureIndex() == -1)return "transparent";
        return this.getMountableData().emissiveTextures().get(getEmissiveTextureIndex());
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
        if (index == -1) index = 0;
        this.entityData.set(MODEL_POSITION, index);
    }
    public int getModelPosition(){
        return this.entityData.get(MODEL_POSITION);
    }

    public void loadMountableData(MountableData data){
        this.setUniqueName(data.uniqueName());
        this.setCustomNameVisible(true);
        this.setCustomName(new TextComponent(data.displayName()));
        this.mountableData = data;
        this.processAttributes(data.attributeMap());
    }

    public void loadMountableData(int index){
        if (index < 0) index = Reference.MOUNTABLE_MANAGER.get().size() - 1;
        if (index >= Reference.MOUNTABLE_MANAGER.get().size()) index = 0;
        loadMountableData(Reference.MOUNTABLE_MANAGER.get().get(index));
    }

    public void loadDefault(String s){
        loadMountableData(Reference.findData(s));
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

    private void processAttributes(Map<String, Double> attributeMap){
        if (this.getAttributes() == null) return;
        Class<Attributes> attriClass = Attributes.class;
        for (Map.Entry<String, Double> entry : attributeMap.entrySet()){
            try {
                String toCompare = FMLEnvironment.production ? Reference.SRG_ATTRIBUTES_MAP.get(entry.getKey()) : entry.getKey();
                Attribute attribute = (Attribute) attriClass.getField(toCompare).get(null);
                this.getAttribute(attribute).setBaseValue(entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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
