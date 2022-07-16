package net.witixin.mountables2.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.witixin.mountables2.Reference;
import net.witixin.mountables2.entity.Mountable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.layer.LayerGlowingAreasGeo;


public class MountableGlowingLayer extends LayerGlowingAreasGeo<Mountable> {


    public MountableGlowingLayer(GeoEntityRenderer<Mountable> renderer) {
        super(renderer, MountableGlowingLayer::getLayerLocation, MountableGlowingLayer::getModelLocation, RenderType::eyes);
    }

    private static ResourceLocation getLayerLocation(Mountable mountable){
        return Reference.rl("textures/" + mountable.getEmissiveTexture() + ".png");
    }
    private static ResourceLocation getModelLocation(Mountable mountable){
        return Reference.rl("geo/" + mountable.getUniqueResourceLocation().getPath() + ".geo.json");
    }
}
