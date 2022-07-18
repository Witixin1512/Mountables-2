package witixin.mountables2.network.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.DistExecutor;
import witixin.mountables2.client.screen.CommandChipScreen;
import witixin.mountables2.client.screen.MountableAIScreen;
import witixin.mountables2.data.MountableInfo;

public class ClientPacketUtils {

    public static DistExecutor.SafeRunnable openScreen(MountableInfo info){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                openChipScreen(info);
            }
        };
    }

    public static DistExecutor.SafeRunnable setUpdateInfoOnClient(MountableInfo info){
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                updateInfo(info);
            }
        };
    }


    public static void openChipScreen(MountableInfo info){
        Minecraft.getInstance().setScreen(new CommandChipScreen(info));
    }

    public static void openAiScreen(MountableInfo info){
        Minecraft.getInstance().setScreen(new MountableAIScreen(info));

    }

    public static void updateInfo(MountableInfo info){
        CommandChipScreen.mountableInfo = info;
    }
}
