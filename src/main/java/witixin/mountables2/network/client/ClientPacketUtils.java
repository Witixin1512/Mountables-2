package witixin.mountables2.network.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.DistExecutor;
import witixin.mountables2.client.screen.CommandChipScreen;

public class ClientPacketUtils {
    public static DistExecutor.SafeRunnable openScreen(int entityId) {
        return () -> openChipScreen(entityId);
    }

    public static void openChipScreen(int entityId) {
        Minecraft.getInstance().setScreen(new CommandChipScreen(entityId));
    }
}
