package witixin.mountables2.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.data.MountableManager;
import witixin.mountables2.entity.Mountable;

public class MountableItem extends Item {
    public MountableItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide && pContext.getPlayer() != null){
            final ItemStack stack = pContext.getItemInHand();
            final Mountable mountable = Mountables2Mod.MOUNTABLE_ENTITY.get().create(pContext.getLevel());
            mountable.setPos(pContext.getClickLocation());
            mountable.setTame(true);
            mountable.setOwnerUUID(pContext.getPlayer().getUUID());
            if (stack.getOrCreateTag().contains("MOUNTABLE")){
                final CompoundTag tagToRead = stack.getTag();
                mountable.loadMountableData(Mountables2Mod.findData(tagToRead.getString("MOUNTABLE")));
                mountable.setModelPosition(tagToRead.getInt("MODEL_POS"));
                mountable.setAbsoluteEmissive(tagToRead.getInt("TEX_POS"));
                if (tagToRead.contains("dead")){
                    mountable.setHealth(1.0f);
                }
                if (tagToRead.contains("locked")){
                    mountable.setLockSwitch(true);
                }
                if (tagToRead.contains("FOLLOW_MODE")){
                    mountable.setFollowMode(tagToRead.getString("FOLLOW_MODE"));
                    mountable.setFlightMode(tagToRead.getString("FLIGHT_MODE"));
                    mountable.setFreeMode(tagToRead.getString("FREE_MODE"));
                    mountable.setWaterMode(tagToRead.getString("WATER_MODE"));
                    mountable.setGroundMode(tagToRead.getString("GROUND_MODE"));
                }
            }
            else {
                //NBT is either invalid or empty here.
                mountable.loadMountableData(MountableManager.get().get(0));
            }
            pContext.getLevel().addFreshEntity(mountable);
            pContext.getPlayer().getItemInHand(pContext.getHand()).shrink(1);
        }
        return super.useOn(pContext);
    }


}
