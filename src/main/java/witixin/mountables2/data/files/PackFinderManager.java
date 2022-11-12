package witixin.mountables2.data.files;

import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import witixin.mountables2.Mountables2Mod;

import java.io.File;
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

        private MountablesRepositorySource(PackType type) {
            this.rootFile = FileUtils.getConfigMountableFolder(type.getDirectory()).getParentFile().getParentFile();
        }

        @Override
        public void loadPacks(Consumer<Pack> pInfoConsumer, Pack.PackConstructor pInfoFactory) {
            if (rootFile.exists()) {
                Pack pack = Pack.create(Mountables2Mod.MODID + ":" + rootFile.getName(), true, () -> new FolderPackResources(rootFile) {
                    public boolean isHidden() {
                        return true;
                    }
                }, pInfoFactory, Pack.Position.BOTTOM, PackSource.DEFAULT);
                if (pack != null) pInfoConsumer.accept(pack);
            }

        }
    }

}
