package witixin.mountables2.data.files;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import witixin.mountables2.Mountables2Mod;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


@Mod.EventBusSubscriber(modid = Mountables2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PackFinderManager {

    @SubscribeEvent
    public static void addPacks(final AddPackFindersEvent event) {
        FileUtils.createPackFromTypeIfNotExists(event.getPackType());
        event.addRepositorySource(new MountablesRepositorySource(event.getPackType()));
    }

    private static class MountablesRepositorySource implements RepositorySource {

        private final File rootFile;
        private final PackType type;

        private MountablesRepositorySource(PackType type) {
            this.rootFile = FileUtils.getConfigMountableFolder(type.getDirectory()).getParentFile().getParentFile();
            this.type = type;
        }

        @Override
        public void loadPacks(Consumer<Pack> pInfoConsumer) {

            Pack.ResourcesSupplier pack$resourcessupplier = (stringName) ->
                new PathPackResources(stringName, rootFile.toPath(), true) {
                @Override
                    public boolean isHidden() {return true;}
                };
            Pack.Info pack$info = Pack.readPackInfo(Mountables2Mod.MODID + ":" + rootFile.getName(), pack$resourcessupplier);
            if (rootFile.exists()) {
                Pack pack = Pack.create(Mountables2Mod.MODID + ":" + rootFile.getName(), Component.translatable("gui.mountables2.pack_name", type.toString().toLowerCase(Locale.ROOT)), true, pack$resourcessupplier, pack$info, type, Pack.Position.BOTTOM, false, PackSource.DEFAULT);
                //TODO TEST THIS ABERRATION
                pInfoConsumer.accept(pack);
            }

        }
    }

}
