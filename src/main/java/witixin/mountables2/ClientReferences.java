package witixin.mountables2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import witixin.mountables2.client.screen.CommandChipScreen;
import witixin.mountables2.client.screen.MountableAIScreen;

public class ClientReferences {

    private static Minecraft get() {
        return Minecraft.getInstance();
    }

    public static Player getClientPlayer() {
        return get().player;
    }

    public static Level getClientLevel() {
        return get().level;
    }

    public static Options getOptions() {
        return get().options;
    }

    public static void openAIScreen(int id) {
        get().setScreen(new MountableAIScreen(id));
    }

    public static void openCommandChipScreen(int id) {
        get().setScreen(new CommandChipScreen(id));
    }

}
