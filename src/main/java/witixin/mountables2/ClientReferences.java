package witixin.mountables2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public static void open(Screen screen) {
        get().setScreen(screen);
    }
}
