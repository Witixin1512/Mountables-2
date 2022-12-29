package witixin.mountables2.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.widgets.ArrowSelectionWidget;
import witixin.mountables2.client.screen.widgets.LinkedArrowWidget;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerUpdateMountFollowTypePacket;
import witixin.mountables2.network.server.ServerUpdateMountModelPacket;
import witixin.mountables2.network.server.ServerUpdateMountTexturePacket;

public class CommandChipScreen extends Screen {

    private static final ResourceLocation ARROW_LEFT = Mountables2Mod.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Mountables2Mod.rl("gui/mountable_right_button.png");
    private static final int PREVIOUS = -1;
    private static final int NEXT = 1;

    public Mountable mount;
    public final int entityId;

    public CommandChipScreen(int mountId) {
        super(Component.literal("commandchipscreen"));
        this.entityId = mountId;
        if (Minecraft.getInstance().level.getEntity(entityId) instanceof Mountable newMount)
            this.mount = newMount;
    }

    @Override
    protected void init() {

        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        //TODO Make sure data is saved and synced somehow
        ArrowSelectionWidget arrowLeftModel = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT,
                () -> PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountModelPacket(entityId, PREVIOUS)));
        ArrowSelectionWidget arrowRightModel = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT,
                () -> PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountModelPacket(entityId, NEXT)));

        ArrowSelectionWidget arrowLeftTex = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT,
                () -> PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountTexturePacket(entityId, PREVIOUS)));
        ArrowSelectionWidget arrowRightTex = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT,
                () -> PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountTexturePacket(entityId, NEXT)));


        final SwitchableWidget WANDER = new SwitchableWidget(0, 0, 100, 20, I18n.get("gui.mountables2.chip.wander"),  pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.WANDER));
        });
        final SwitchableWidget STAY = new SwitchableWidget(0, 40, 100, 20, I18n.get("gui.mountables2.chip.stay"),  pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.STAY));
        });
        final SwitchableWidget FOLLOW = new SwitchableWidget(0, 80, 100, 20, I18n.get("gui.mountables2.chip.follow"), pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.FOLLOW));
        });

        SwitchableWidget[] selectable = new SwitchableWidget[3];
        selectable[Mountable.FOLLOW] = FOLLOW;
        selectable[Mountable.WANDER] = WANDER;
        selectable[Mountable.STAY] = STAY;

        this.addRenderableWidget(new LinkedSwitchableWidget(posX / 2 - 50, posY / 2 - 100 , 100, 35 * selectable.length, mount.getFollowMode(), selectable));

        if (!mount.getLockSwitch()) {
            this.addRenderableWidget(new LinkedArrowWidget(posX / 2 - 50, posY / 2 + 40, 100, 16, arrowLeftModel, arrowRightModel, I18n.get("gui.mountables2.chip.model")));
        }

        this.addRenderableWidget(new LinkedArrowWidget(posX / 2 - 50, posY / 2 + (mount.getLockSwitch() ? 30 : 20), 100, 16, arrowLeftTex, arrowRightTex, I18n.get("gui.mountables2.chip.texture")));

        this.addRenderableWidget(new SwitchableWidget(posX / 2 - 50, (posY - 256) / 2 + 200, 100, 20, I18n.get("gui.mountables2.ai.ai"),  pButton -> {
            minecraft.setScreen(new MountableAIScreen(entityId));
        }));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
