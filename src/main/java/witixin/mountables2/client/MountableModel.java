package witixin.mountables2.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;

public class MountableModel extends AnimatedGeoModel<Mountable> {
    @Override
    public ResourceLocation getModelLocation(Mountable object) {
        return Mountables2Mod.rl("geo/" + object.getUniqueResourceLocation().getPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Mountable object) {
        return Mountables2Mod.rl("textures/" + object.getUniqueResourceLocation().getPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Mountable animatable) {
        return Mountables2Mod.rl("animations/" + animatable.getUniqueResourceLocation().getPath() + ".animation.json");
    }
}
