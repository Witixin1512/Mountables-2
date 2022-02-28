package net.witixin.mountables2.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.entity.goal.MountableFloatGoal;
import net.witixin.mountables2.entity.goal.MountableFollowGoal;
import net.witixin.mountables2.entity.goal.MountableJumpControl;
import net.witixin.mountables2.entity.goal.MountableWanderGoal;
import net.witixin.mountables2.network.ClientOpenScreenPacket;
import net.witixin.mountables2.network.PacketHandler;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Mountable extends TamableAnimal implements IAnimatable, PlayerRideableJumping {

    public static final String DEFAULT_NAME = "companion_block";

    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> FOLLOW_TYPE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> MODEL_POSITION = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<String> FREE_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> GROUND_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WATER_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> FLIGHT_MODE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);

    public static final EntityDataAccessor<Boolean> CAN_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);

    private AnimationFactory factory = new AnimationFactory(this);
    private MountableData mountableData;

    public Mountable(EntityType<Mountable> mountableEntityEntityType, Level level) {
        super(Reference.MOUNTABLE_ENTITY.get(), level);
        this.moveControl = new MountableControl(this);
        this.jumpControl = new MountableJumpControl(this);
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

    @Override
    public boolean isControlledByLocalInstance() {
        return isVehicle();
    }
    public boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return super.isInvulnerableTo(pSource) || pSource.getMsgId().matches(DamageSource.FALL.getMsgId()) || pSource.getMsgId().matches(DamageSource.DROWN.getMsgId());
    }


    @Override
    public void travel(Vec3 vectoring) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.canBeControlledByRider()) {
                LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
                this.setYRot(livingentity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                float f = livingentity.xxa * 0.5F;
                float f1 = livingentity.zza;
                this.flyingSpeed = this.getSpeed() * 0.1F;
                if (this.getGroundMode().matches(GROUND_MOVEMENT.HOP.name()) && this.isOnGround() && !this.jumping){
                    double d0 = this.getBlockJumpFactor();
                    double d1 = d0 + this.getJumpBoostPower();
                    this.setDeltaMovement(f, d1, f1);
                    this.setJumping(true);
                    this.hasImpulse = true;
                    net.minecraftforge.common.ForgeHooks.onLivingJump(this);
                }
                if (this.isControlledByLocalInstance()) {
                    if (this.isWalking()){
                        this.setSpeed((float) this.getCurrentSpeed());
                        super.travel(new Vec3(f, vectoring.y, f1));
                    }

                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                if (this.onGround) {
                    this.jumping = false;
                }

                this.calculateEntityAnimation(this, false);
                this.tryCheckInsideBlocks();
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(vectoring);
            }
        }
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
        this.entityData.define(CAN_FLY, false);
        this.entityData.define(CAN_SWIM, false);
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

    public boolean isWalking(){
        return this.getGroundMode().matches(GROUND_MOVEMENT.SLOW_WALK.name()) || this.getGroundMode().matches(GROUND_MOVEMENT.WALK.name());
    }

    private double getCurrentSpeed(){
         double defaultSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
         if (this.isWalking()){
             if (this.getGroundMode().matches(GROUND_MOVEMENT.SLOW_WALK.name())){
                 return defaultSpeed * 0.5;
             }
             else if (this.getGroundMode().matches(GROUND_MOVEMENT.WALK.name())){
                 return defaultSpeed;
             }
         }
         return 0.5f;
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
            ItemStack mountable_item = new ItemStack(Reference.MOUNTABLE.get());
            mountable_item.getOrCreateTag().putString("MOUNTABLE", this.getUniqueResourceLocation().getPath());
            mountable_item.getTag().putString("FOLLOW_MODE", this.getFollowMode());
            mountable_item.getTag().putUUID("OWNER", this.getOwnerUUID());
            mountable_item.getTag().putInt("MODEL_POS", this.getModelPosition());
            mountable_item.getTag().putInt("TEX_POS", this.getEmissiveTextureIndex());
            this.spawnAtLocation(mountable_item);
            return InteractionResult.SUCCESS;
        }
        if (itemstack.getItem().equals(Reference.COMMAND_CHIP.get()) && !pPlayer.level.isClientSide && this.getOwnerUUID().equals(pPlayer.getUUID())){
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) pPlayer), new ClientOpenScreenPacket(this.getUUID(), this.getFollowMode(), this.getFreeMode(), this.getGroundMode(), this.getWaterMode(), this.getFlightMode(), this.canFreeSwim(), this.canFreeFly()));
            return InteractionResult.SUCCESS;
        }
        if (!this.isVehicle() && !pPlayer.level.isClientSide && pPlayer.getMainHandItem().getItem() != Reference.COMMAND_CHIP.get()) {
            this.doPlayerRide(pPlayer);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(pPlayer, pHand);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.getAnimatable() instanceof Mountable mountable){
            if (event.isMoving()) {
                if (mountable.isInWater()) event.getController().setAnimation(new AnimationBuilder().addAnimation("swim", true));
                if (mountable.onGround) event.getController().setAnimation(new AnimationBuilder().addAnimation("walk", true));
                if (!mountable.onGround && this.entityData.get(IS_FLYING)) event.getController().setAnimation(new AnimationBuilder().addAnimation("fly", true));
                    return PlayState.CONTINUE;
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
        this.mountableData = data;
    }

    public void loadMountableData(int index){
        if (index < 0) index = Reference.MOUNTABLE_MANAGER.get().size() - 1;
        if (index >= Reference.MOUNTABLE_MANAGER.get().size()) index = 0;
        loadMountableData(Reference.MOUNTABLE_MANAGER.get().get(index));
    }

    public void loadDefault(CompoundTag tag){
        loadMountableData(Reference.findData(tag.getString("MOUNTABLE").toLowerCase()));
    }

    public boolean canFreeFly(){return entityData.get(CAN_FLY);}
    public boolean canFreeSwim(){return entityData.get(CAN_SWIM);}
    public void setFreeFly(boolean value){this.entityData.set(CAN_FLY, value);}
    public void setFreeSwim(boolean value){this.entityData.set(CAN_SWIM, value);}

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
    public void onPlayerJump(int pJumpPower) {

    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int pJumpPower) {
        if (pJumpPower >= 30){
            this.jumpFromGround();
        }
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
