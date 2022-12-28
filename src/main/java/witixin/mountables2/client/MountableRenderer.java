package witixin.mountables2.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import witixin.mountables2.entity.Mountable;

public class MountableRenderer extends GeoEntityRenderer<Mountable> {

    public static final MountableModel MODEL = new MountableModel();

    public MountableRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, MODEL);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

}
