package net.witixin.mountables2.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.entity.Mountable;

public class MountableItem extends Item {
    public MountableItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide){
            ItemStack stack = pContext.getItemInHand();
            Mountable mountable = new Mountable(Reference.MOUNTABLE_ENTITY.get(), pContext.getLevel());
            mountable.setPos(pContext.getClickLocation());
            mountable.setTame(true);
            mountable.setOwnerUUID(pContext.getPlayer().getUUID());
            if (!pContext.getItemInHand().getOrCreateTag().contains("MOUNTABLE")){
                mountable.loadDefault(Mountable.DEFAULT_NAME);
            }
            if (stack.getOrCreateTag().contains("FOLLOW_MODE")){
                mountable.setFollowMode(stack.getTag().getString("FOLLOW_MODE"));
                mountable.setModelPosition(stack.getTag().getInt("MODEL_POS"));
                mountable.setAbsoluteEmissive(stack.getTag().getInt("TEX_POS"));
                mountable.setFlightMode(stack.getTag().getString("FLIGHT_MODE"));
                mountable.setFreeMode(stack.getTag().getString("FREE_MODE"));
                mountable.setWaterMode(stack.getTag().getString("WATER_MODE"));
                mountable.setGroundMode(stack.getTag().getString("GROUND_MODE"));
            }
            if (stack.getTag().contains("dead")){
                mountable.setHealth(1.0f);
            }
            pContext.getLevel().addFreshEntity(mountable);
            pContext.getPlayer().getItemInHand(pContext.getHand()).shrink(1);
        }
        return super.useOn(pContext);
    }
    public void saveEntity(Mountable mountable){

    }

}
