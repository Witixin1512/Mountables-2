package witixin.mountables2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;

import javax.annotation.Nullable;

public class MountableRenderer extends GeoEntityRenderer<Mountable> {

    public static final MountableModel MODEL = new MountableModel();

    public MountableRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, MODEL);
        this.addRenderLayer(new MountableRenderLayer(this));
    }


    private static class MountableRenderLayer extends GeoRenderLayer<Mountable> {

        public MountableRenderLayer(GeoRenderer<Mountable> renderer) {
            super(renderer);
        }

        @Nullable
        protected RenderType getRenderType(Mountable animatable) {
            String location = animatable.getEmissiveTexture();
            if (location.equals("transparent")) return null;
            return RenderType.eyes(Mountables2Mod.rl("textures/" + location + ".png"));
        }

        @Override
        public void render(PoseStack poseStack, Mountable animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderType type = getRenderType(animatable);

            if (type != null) {
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, type,
                        bufferSource.getBuffer(type), partialTick, 15728640, OverlayTexture.NO_OVERLAY,
                        1, 1, 1, 1);
            }
        }
    }
}
