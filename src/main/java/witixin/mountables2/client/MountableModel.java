package witixin.mountables2.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;

public class MountableModel extends GeoModel<Mountable> {

    @Override
    public ResourceLocation getModelResource(Mountable object) {
        return Mountables2Mod.rl("geo/" + object.getUniqueResourceLocation().getPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Mountable object) {
        return Mountables2Mod.rl("textures/" + object.getUniqueResourceLocation().getPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(Mountable animatable) {
        return Mountables2Mod.rl("animations/" + animatable.getUniqueResourceLocation().getPath() + ".animation.json");
    }

    @Override
    public RenderType getRenderType(Mountable animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
