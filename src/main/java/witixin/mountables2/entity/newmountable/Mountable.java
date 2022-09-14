package witixin.mountables2.entity.newmountable;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.CommandChipScreen;
import witixin.mountables2.data.MountableData;
import witixin.mountables2.data.MountableManager;
import witixin.mountables2.entity.newmountable.movement.MountMovement;
import witixin.mountables2.entity.newmountable.movement.MountTravel;
import witixin.mountables2.entity.newmountable.movement.MovementRegistry;

import java.util.List;
import java.util.Map;

public class Mountable extends TamableAnimal implements IAnimatable, PlayerRideableJumping {

    public static final EntityDataAccessor<Float> ENTITY_WIDTH = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> ENTITY_HEIGHT = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<String> UNIQUE_NAME = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_FLY = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_SWIM = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MINOR_MOVEMENT_WALK = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> EMISSIVE_TEXTURE = SynchedEntityData.defineId(Mountable.class, EntityDataSerializers.INT);

    private final AnimationFactory factory = new AnimationFactory(this);
    private MountableData mountableData;
    private MountTravel currentTravelMethod = MovementRegistry.INSTANCE.getMovement(MountTravel.Major.WALK, MountTravel.Minor.NONE);
    private boolean isFlying;

    public Mountable(EntityType type, Level level) {
        super(Mountables2Mod.MOUNTABLE_ENTITY.get(), level);
        dimensions = getDimensions(Pose.STANDING); //pose not important
    }

    private int getEmissiveTextureIndex() {
        return this.entityData.get(EMISSIVE_TEXTURE);
    }

    public String getEmissiveTexture() {
        final List<String> mountableData = getMountableData().emissiveTextures();
        final int textIndex = getEmissiveTextureIndex();
        if (textIndex == -1 || textIndex >= mountableData.size()) return "transparent";
        return mountableData.get(getEmissiveTextureIndex());
    }

    public void setEmissiveTexture(int index) {
        index += getEmissiveTextureIndex();
        if (index == -2) index = this.getMountableData().emissiveTextures().size() - 1;
        if (index >= this.getMountableData().emissiveTextures().size()) index = -1;
        this.entityData.set(EMISSIVE_TEXTURE, index);
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }

    public boolean canFly() {
        return getMountableData().aiModes()[MountableManager.FLY];
    }

    public MountableData getMountableData() {
        if (this.mountableData == null) {
            this.mountableData = Mountables2Mod.findData(getUniqueResourceLocation().getPath());
            loadMountableData(mountableData);
        }
        return this.mountableData;
    }

    public ResourceLocation getUniqueResourceLocation() {
        return Mountables2Mod.rl(this.entityData.get(UNIQUE_NAME));
    }

    public void loadMountableData(MountableData data) {
        this.setUniqueName(data.uniqueName());
        this.setCustomNameVisible(true);
        this.setCustomName(new TextComponent(data.displayName()));
        this.processAttributes(data.attributeMap());
        this.entityData.set(ENTITY_WIDTH, (float) data.width());
        this.entityData.set(ENTITY_HEIGHT, (float) data.height());
    }

    public void setUniqueName(String s) {
        this.entityData.set(UNIQUE_NAME, s);
    }

    //what does this do ? //TODO check for functionality
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

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {


        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (!pPlayer.level.isClientSide && pPlayer.isShiftKeyDown() && this.getOwnerUUID().equals(pPlayer.getUUID())) {
            this.remove(RemovalReason.DISCARDED);
            //TODO this.dropMountableItem();
            return InteractionResult.SUCCESS;
        }
        if (itemstack.getItem().equals(Mountables2Mod.COMMAND_CHIP.get()) && pPlayer.level.isClientSide && this.getOwnerUUID().equals(pPlayer.getUUID())) {
            Minecraft.getInstance().setScreen(new CommandChipScreen(getId()));
            //TODO open safe without minecraft call
            return InteractionResult.sidedSuccess(true);
        }
        if (!this.isVehicle() && !pPlayer.level.isClientSide && pPlayer.getMainHandItem().getItem() != Mountables2Mod.COMMAND_CHIP.get()) {
            if (!this.level.isClientSide) {
                pPlayer.setYRot(this.getYRot());
                pPlayer.setXRot(this.getXRot());
                pPlayer.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(pPlayer, pHand);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.entityData.get(ENTITY_WIDTH), this.entityData.get(ENTITY_HEIGHT));
    }

    public boolean canWalk() {
        return getMountableData().aiModes()[MountableManager.WALK];
    }

    public boolean canSwim() {
        return getMountableData().aiModes()[MountableManager.FLY];
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

    @Override
    public void onPlayerJump(int pJumpPower) {
        currentTravelMethod.getMovement().travel(this, MountMovement.MovementType.SPACEBAR);
    }

    @Override
    public boolean canJump() {
        return !currentTravelMethod.getMinor().equals(MountTravel.Minor.SINK);
    }

    @Override
    public void handleStartJump(int pJumpPower) {

    }

    @Override
    public void handleStopJump() {

    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));

    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.getAnimatable() instanceof Mountable mountable) {
            if (event.isMoving()) {
                if (currentTravelMethod.getMajor().equals(MountTravel.Major.SWIM)) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("swim", true));
                } else if (currentTravelMethod.getMajor().equals(MountTravel.Major.WALK)) {
                    //TODO JUMP AND LAND
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("walk", true));
                } else if (currentTravelMethod.getMajor().equals(MountTravel.Major.FLY)) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("fly", true));
                }
                return PlayState.CONTINUE;
            } else {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", true));
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(UNIQUE_NAME, MountableManager.get().get(0).uniqueName());
        this.entityData.define(EMISSIVE_TEXTURE, -1);
        this.entityData.define(MINOR_MOVEMENT_FLY, MountTravel.Minor.NONE.name());
        this.entityData.define(MINOR_MOVEMENT_SWIM, MountTravel.Minor.NONE.name());
        this.entityData.define(MINOR_MOVEMENT_WALK, MountTravel.Minor.NONE.name());
        this.entityData.define(ENTITY_WIDTH, 1.0f);
        this.entityData.define(ENTITY_HEIGHT, 1.0f);
    }

    //called from interaction screen with entity
    public void setMinorMovement(MountTravel.Major major, MountTravel.Minor minor) {
        this.entityData.set(getEDAForMinor(major), minor.name());
    }

    private EntityDataAccessor<String> getEDAForMinor(MountTravel.Major major) {
        return switch (major) {
            case FLY -> MINOR_MOVEMENT_FLY;
            case SWIM -> MINOR_MOVEMENT_SWIM;
            case WALK -> MINOR_MOVEMENT_WALK;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWaterOrBubble()) {
            currentTravelMethod = MovementRegistry.INSTANCE.getMovement(MountTravel.Major.SWIM, getMinorMovement(MountTravel.Major.SWIM));
        } else if (this.isOnGround()) {
            if (this.isFlying())
                this.setFlying(false);
            currentTravelMethod = MovementRegistry.INSTANCE.getMovement(MountTravel.Major.WALK, getMinorMovement(MountTravel.Major.WALK));
        } else if (this.isFlying()) {
            currentTravelMethod = MovementRegistry.INSTANCE.getMovement(MountTravel.Major.FLY, getMinorMovement(MountTravel.Major.FLY));
        }
    }

    public MountTravel.Minor getMinorMovement(MountTravel.Major major) {
        return MountTravel.from(this.entityData.get(getEDAForMinor(major)));
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flag) {
        isFlying = flag;
    }

    private boolean lockSwitch;

    public boolean getLockSwitch() {
        return lockSwitch;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("unique_mountable_name", getUniqueResourceLocation().getPath());
//        tag.putString("follow_mode", getFollowMode());
        tag.putInt("emissive_texture", getEmissiveTextureIndex());
//        tag.putInt("model_position", getModelPosition());
//        tag.putString("free_mode", this.getFreeMode());
//        tag.putString("ground_mode", this.getGroundMode());
//        tag.putString("water_mode", this.getWaterMode());
//        tag.putString("flight_mode", this.getFlightMode());
//        tag.putBoolean("free_flight", this.canFreeFly());
//        tag.putBoolean("free_swim", this.canFreeSwim());
//        tag.putBoolean("flying", this.entityData.get(IS_FLYING));
        tag.putFloat("mountable_width", this.entityData.get(ENTITY_WIDTH));
        tag.putFloat("mountable_height", this.entityData.get(ENTITY_HEIGHT));
        tag.putBoolean("lock_switch", lockSwitch);
    }

    public boolean isLockSwitch() {
        return lockSwitch;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setUniqueName(tag.getString("unique_mountable_name"));
//        this.setFollowMode(tag.getString("follow_mode"));
//        this.setAbsoluteEmissive(tag.getInt("emissive_texture"));
//        this.setModelPosition(tag.getInt("model_position"));
//        this.setFreeMode(tag.getString("free_mode"));
//        this.setGroundMode(tag.getString("ground_mode"));
//        this.setWaterMode(tag.getString("water_mode"));
//        this.setFlightMode(tag.getString("flight_mode"));
//        this.setFreeFly(tag.getBoolean("free_flight"));
//        this.setFreeSwim(tag.getBoolean("free_swim"));
//        this.entityData.set(IS_FLYING, tag.getBoolean("flying"));
        this.entityData.set(ENTITY_WIDTH, tag.getFloat("mountable_width"));
        this.entityData.set(ENTITY_HEIGHT, tag.getFloat("mountable_height"));
        this.lockSwitch = tag.getBoolean("lock_switch");
    }
}
