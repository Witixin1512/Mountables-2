package witixin.mountables2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.entity.Mountable;

public class MountableLayer extends GeoLayerRenderer<Mountable> {
    public MountableLayer(IGeoRenderer<Mountable> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(Mountable entityIn) {
        return Mountables2Mod.rl("textures/" + entityIn.getEmissiveTexture() + ".png");
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Mountable entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        //renderCopyModel(this.getEntityModel(), getEntityTexture(entityLivingBaseIn), matrixStackIn, bufferIn, packedLightIn, entityLivingBaseIn, partialTicks,  0f, 0f, 0);
        RenderType renderType = RenderType.entityTranslucentCull(getEntityTexture(entityLivingBaseIn));
        this.getRenderer().render(this.getEntityModel().getModel(getEntityModel().getModelLocation(entityLivingBaseIn)), entityLivingBaseIn, partialTicks, renderType, matrixStackIn, bufferIn, bufferIn.getBuffer(renderType), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation) {
        return RenderType.entityTranslucentCull(textureLocation);
    }
}
