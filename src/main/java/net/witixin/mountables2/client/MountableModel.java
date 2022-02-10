package net.witixin.mountables2.client;

import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.entity.Mountable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MountableModel extends AnimatedGeoModel<Mountable> {
    @Override
    public ResourceLocation getModelLocation(Mountable object) {
        return Reference.rl("geo/" + object.getUniqueResourceLocation().getPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Mountable object) {
        return Reference.rl("textures/" + object.getUniqueResourceLocation().getPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Mountable animatable) {
        return Reference.rl("animations/" + animatable.getUniqueResourceLocation().getPath() + ".animation.json");
    }
}
