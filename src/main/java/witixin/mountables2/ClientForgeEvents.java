package witixin.mountables2;

import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.entity.movement.KeyStrokeMovement;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerHandleKeyPressMovement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Mountables2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private final static Map<String, Supplier<SoundEvent>> substitute = Util.make(() -> {
        Map<String, Supplier<SoundEvent>> map = new HashMap<>();
        map.put("idle", Mountables2Mod.EMPTY_SOUND_EVENT);
        map.put("walk", () -> SoundEvents.GRASS_STEP);
        map.put("hurt", () -> SoundEvents.GENERIC_HURT);
        map.put("death", () -> SoundEvents.GENERIC_DEATH);
        map.put("swim", () -> SoundEvents.GENERIC_SWIM);
        map.put("splash", () -> SoundEvents.GENERIC_SPLASH);
        return map;
    });

    @SubscribeEvent
    public static void soundEvent(final PlaySoundEvent event) {
        WeighedSoundEvents weighedsoundevents = event.getSound().resolve(event.getEngine().soundManager);
        final String toUse = event.getSound().getLocation().getPath().split("\\.")[1];
        if (event.getSound().getLocation().getNamespace().matches(Mountables2Mod.MODID) && weighedsoundevents == null) {
            if (substitute.containsKey(toUse)) {
                event.setSound(SimpleSoundInstance.forAmbientAddition(substitute.get(toUse).get()));
            }
        }

    }

    @SubscribeEvent
    public static void keyPressListener(final InputEvent.Key event) {
        if (ClientReferences.getClientPlayer() != null && ClientReferences.getClientPlayer().isPassenger() && ClientReferences.getClientPlayer().getVehicle() instanceof Mountable mount) {
            boolean forwards = ClientReferences.getOptions().keyUp.isDown();
            boolean backwards = ClientReferences.getOptions().keyDown.isDown();
            boolean left = ClientReferences.getOptions().keyLeft.isDown();
            boolean right = ClientReferences.getOptions().keyRight.isDown();
            boolean spacebar = ClientReferences.getOptions().keyJump.isDown();

            KeyStrokeMovement movement = new KeyStrokeMovement(forwards, backwards, left, right, spacebar);

            if (!mount.getKeyStrokeMovement().equals(movement)) {
                mount.setKeyStrokeMovement(movement);
                PacketHandler.INSTANCE.sendToServer(new ServerHandleKeyPressMovement(movement));
            }
        }
    }

}
