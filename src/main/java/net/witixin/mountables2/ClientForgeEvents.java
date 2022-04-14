package net.witixin.mountables2;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    static Map<String, SoundEvent> substitute = new HashMap<>();

    @SubscribeEvent
    public static void soundEvent(final PlaySoundEvent event){
        WeighedSoundEvents weighedsoundevents = event.getSound().resolve(event.getEngine().soundManager);
        final String toUse = event.getSound().getLocation().getPath().split("\\.")[1];
        if (event.getSound().getLocation().getNamespace().matches(Reference.MODID) && weighedsoundevents == null) {
            if (substitute.containsKey(toUse)){
                event.setSound(SimpleSoundInstance.forAmbientAddition(substitute.get(toUse)));
            }
        }

    }

    static void setup(){
        substitute.put("idle", Reference.EMPTY_SOUND_EVENT.get());
        substitute.put("walk", SoundEvents.GRASS_STEP);
        substitute.put("hurt", SoundEvents.GENERIC_HURT);
        substitute.put("death", SoundEvents.GENERIC_DEATH);
        substitute.put("swim", SoundEvents.GENERIC_SWIM);
        substitute.put("splash", SoundEvents.GENERIC_SPLASH);
    }
}
