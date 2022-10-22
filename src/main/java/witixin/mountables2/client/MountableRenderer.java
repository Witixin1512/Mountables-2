package witixin.mountables2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import witixin.mountables2.entity.Mountable;

public class MountableRenderer extends GeoEntityRenderer<Mountable> {
    public MountableRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MountableModel());
        this.addLayer(new MountableGlowingLayer(this));
    }

    @Override
    public RenderType getRenderType(Mountable animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
