package witixin.mountables2.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;

public class MountableModel extends GeoModel<Mountable> {


    //I'm not using the defaulted GeoModel as dynamic
    @Override
    public ResourceLocation getModelResource(Mountable object) {
        return Mountables2Mod.rl("geo/" + object.getUniqueName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Mountable object) {
        return Mountables2Mod.rl("textures/" + object.getUniqueName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(Mountable animatable) {
        return Mountables2Mod.rl("animations/" + animatable.getUniqueName() + ".animation.json");
    }

    @Override
    public RenderType getRenderType(Mountable animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
