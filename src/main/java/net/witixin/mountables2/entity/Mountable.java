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
import net.witixin.mountables2.entity.goal.MountableFollowGoal;
import net.witixin.mountables2.entity.goal.MountableWanderGoal;
import net.witixin.mountables2.network.ClientOpenScreenPacket;
import net.witixin.mountables2.network.PacketHandler;
import org.antlr.v4.codegen.model.Sync;
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

public class Mountable extends TamableAnimal implements IAnimatable {

    public static final String DEFAULT_NAME = "companion_block";

    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> FOLLOW_TYPE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> MODEL_POSITION = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);

    private AnimationFactory factory = new AnimationFactory(this);
    private MountableData mountableData;
    private float jumpingScaling = 0.0f;

    public Mountable(EntityType<Mountable> mountableEntityEntityType, Level level) {
        super(Reference.MOUNTABLE_ENTITY.get(), level);
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(3, new MountableFollowGoal(this));
        this.goalSelector.addGoal(3, new MountableWanderGoal(this));
    }


    @Override
    protected void defineSynchedData(){
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, DEFAULT_NAME);
        this.entityData.define(IS_FLYING, false);
        this.entityData.define(FOLLOW_TYPE, "FOLLOW");
        this.entityData.define(EMISSIVE_TEXTURE, -1);
        this.entityData.define(MODEL_POSITION, 0);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
        this.setFollowMode(tag.getString("follow_mode"));
        this.setEmissiveTexture(tag.getInt("emmisive_texture"));
        this.setModelPosition(tag.getInt("model_position"));
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putString("unique_mountable_name", getUniqueResourceLocation().getPath());
        tag.putString("follow_mode", getFollowMode());
        tag.putInt("emissive_texture", getEmissiveTextureIndex());
        tag.putInt("model_position", getModelPosition());
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
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
                if (f1 <= 0.0F) {
                    f1 *= 0.25F;
                }

                if (this.onGround && this.jumpingScaling == 0.0F) {
                    f = 0.0F;
                    f1 = 0.0F;
                }

                if (this.jumpingScaling > 0.0F && this.onGround) {
                    Vec3 vec3 = this.getDeltaMovement();
                    this.setDeltaMovement(vec3.x, this.getJumpBoostPower(), vec3.z);
                    this.hasImpulse = true;
                    net.minecraftforge.common.ForgeHooks.onLivingJump(this);
                    if (f1 > 0.0F) {
                        float f2 = Mth.sin(this.getYRot() * ((float) Math.PI / 180F));
                        float f3 = Mth.cos(this.getYRot() * ((float) Math.PI / 180F));
                        this.setDeltaMovement(this.getDeltaMovement().add((double) (-0.4F * f2 * this.jumpingScaling), 0.0D, (double) (0.4F * f3 * this.jumpingScaling)));
                    }

                    this.jumpingScaling = 0.0F;
                }

                this.flyingSpeed = this.getSpeed() * 0.1F;
                if (this.isControlledByLocalInstance()) {
                    this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vec3((double) f, pTravelVector.y, (double) f1));
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                if (this.onGround) {
                    this.jumpingScaling = 0.0F;
                }

                this.calculateEntityAnimation(this, false);
                this.tryCheckInsideBlocks();
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(pTravelVector);
            }
        }
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
            float f3 = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
            float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
            MountableData data = this.getMountableData();
            pPassenger.setPos(this.getX() + data.position()[0], this.getY() + data.position()[1], this.getZ() + data.position()[2]);
            if (pPassenger instanceof LivingEntity) {
                ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
            }

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
        if (itemstack.getItem().equals(Reference.COMMAND_CHIP.get()) && !pPlayer.level.isClientSide){
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) pPlayer), new ClientOpenScreenPacket(this.getFollowMode(), this.getUUID(), MountableData.listToNBT(Reference.MOUNTABLE_MANAGER.get())));
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


    public static enum FOLLOW_TYPES {
        FOLLOW,
        STAY,
        WANDER;
    }


    public static enum NON_RIDER {
        WALK, SWIM, SLOW_WALK, JUMP, FLY;
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
