package net.witixin.mountables2;

import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private final static Map<String, Supplier<SoundEvent>> substitute = Util.make( () ->{
        Map<String, Supplier<SoundEvent>> map = new  HashMap<>();
        map.put("idle", Reference.EMPTY_SOUND_EVENT);
        map.put("walk", () -> SoundEvents.GRASS_STEP);
        map.put("hurt", () -> SoundEvents.GENERIC_HURT);
        map.put("death", () -> SoundEvents.GENERIC_DEATH);
        map.put("swim", () -> SoundEvents.GENERIC_SWIM);
        map.put("splash", () -> SoundEvents.GENERIC_SPLASH);
        return map;
    } );

    @SubscribeEvent
    public static void soundEvent(final PlaySoundEvent event){
        WeighedSoundEvents weighedsoundevents = event.getSound().resolve(event.getEngine().soundManager);
        final String toUse = event.getSound().getLocation().getPath().split("\\.")[1];
        if (event.getSound().getLocation().getNamespace().matches(Reference.MODID) && weighedsoundevents == null) {
            if (substitute.containsKey(toUse)){
                event.setSound(SimpleSoundInstance.forAmbientAddition(substitute.get(toUse).get()));
            }
        }

    }

}
