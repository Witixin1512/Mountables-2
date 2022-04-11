package net.witixin.mountables2.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;

public class CommandChip extends Item {
    public CommandChip(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public boolean canBeHurtBy(DamageSource pDamageSource) {
        return super.canBeHurtBy(pDamageSource) && !pDamageSource.isExplosion();
    }
}
