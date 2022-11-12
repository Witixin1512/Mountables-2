package witixin.mountables2.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.layer.LayerGlowingAreasGeo;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;


public class MountableGlowingLayer extends LayerGlowingAreasGeo<Mountable> {

    public MountableGlowingLayer(GeoEntityRenderer<Mountable> renderer) {
        super(renderer, MountableGlowingLayer::getLayerLocation, MountableGlowingLayer::getModelLocation, RenderType::eyes);
    }

    private static ResourceLocation getLayerLocation(Mountable mountable) {
        return Mountables2Mod.rl("textures/" + mountable.getEmissiveTexture() + ".png");
    }

    private static ResourceLocation getModelLocation(Mountable mountable) {
        return Mountables2Mod.rl("geo/" + mountable.getUniqueResourceLocation().getPath() + ".geo.json");
    }
}
