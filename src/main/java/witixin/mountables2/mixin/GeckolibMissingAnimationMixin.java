package witixin.mountables2.mixin;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.RawAnimation;
import witixin.mountables2.entity.Mountable;

import java.io.PrintStream;
import java.util.Queue;


@Mixin(AnimationProcessor.class)
@Debug(export = true, print = true)
public class GeckolibMissingAnimationMixin {

    private GeoAnimatable geoAnimatable;

    /**
     * Should theoretically unconditionally shutup geckolib's printer.
     */
    @Redirect(method = "Lsoftware/bernie/geckolib/core/animation/AnimationProcessor;buildAnimationQueue(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;" +
            "Lsoftware/bernie/geckolib/core/animation/RawAnimation;)Ljava/util/Queue;", at = @At(value = "INVOKE",
            target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"), remap = false)
    private void mountables2_redirectPrintout(PrintStream printStream, String toPrint) {
        if (!(geoAnimatable instanceof Mountable mountable)) {
            printStream.println(toPrint);
        }
    }

    @Inject(method = "Lsoftware/bernie/geckolib/core/animation/AnimationProcessor;buildAnimationQueue(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;" +
            "Lsoftware/bernie/geckolib/core/animation/RawAnimation;)Ljava/util/Queue;", cancellable = false, at = @At(value = "INVOKE",
            target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"), remap = false)
    private void mountables2_injectAtAnimation(GeoAnimatable geoAnimatable, RawAnimation animation, CallbackInfoReturnable<Queue> callbackInfo) {
        this.geoAnimatable = geoAnimatable;
    }
}
