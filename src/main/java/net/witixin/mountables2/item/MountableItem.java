package net.witixin.mountables2.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
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
            if (!pContext.getItemInHand().getOrCreateTag().contains("MOUNTABLE")){
                pContext.getItemInHand().getTag().putString("MOUNTABLE", Mountable.DEFAULT_NAME);
            }

            Mountable mountable = new Mountable(Reference.MOUNTABLE_ENTITY.get(), pContext.getLevel());
            mountable.setPos(pContext.getClickLocation());
            mountable.setTame(true);
            mountable.setOwnerUUID(pContext.getPlayer().getUUID());
            if (pContext.getItemInHand().getOrCreateTag().contains("FOLLOW_MODE")){
                mountable.setFollowMode(pContext.getItemInHand().getTag().getString("FOLLOW_MODE"));
            }
            pContext.getLevel().addFreshEntity(mountable);
            mountable.loadDefault(pContext.getItemInHand().getOrCreateTag());
            pContext.getPlayer().getItemInHand(pContext.getHand()).shrink(1);
        }
        return super.useOn(pContext);
    }


}
