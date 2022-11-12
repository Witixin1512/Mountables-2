package witixin.mountables2.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import witixin.mountables2.Mountables2Mod;
import witixin.mountables2.client.screen.widgets.ArrowSelectionWidget;
import witixin.mountables2.client.screen.widgets.LinkedArrowWidget;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.Mountable;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerUpdateMountFollowTypePacket;

public class CommandChipScreen extends Screen {

    private static final ResourceLocation ARROW_LEFT = Mountables2Mod.rl("gui/mountable_left_button.png");
    private static final ResourceLocation ARROW_RIGHT = Mountables2Mod.rl("gui/mountable_right_button.png");
    public Mountable mount;
    public final int entityId;

    public CommandChipScreen(int mountId) {
        super(new TextComponent("commandchipscreen"));
        this.entityId = mountId;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft.level.getEntity(entityId) instanceof Mountable mount)
            this.mount = mount;
        else throw new NullPointerException("given entity was not a mount !");

        if (this instanceof MountableAIScreen)
            return;//prevent drawing all the other buttons from the mountable ai screen which extends this class

        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        ArrowSelectionWidget arrowLeftModel = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        ArrowSelectionWidget arrowRightModel = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);

        ArrowSelectionWidget arrowLeftTex = new ArrowSelectionWidget(0, 0, 16, 16, ARROW_LEFT);
        ArrowSelectionWidget arrowRightTex = new ArrowSelectionWidget(84, 0, 16, 16, ARROW_RIGHT);


        final SwitchableWidget WANDER = new SwitchableWidget(0, 0, 100, 20, I18n.get("gui.mountables2.chip.wander"), "wander", pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.WANDER));
            mount.setFollowMode(Mountable.WANDER);
        });
        final SwitchableWidget STAY = new SwitchableWidget(0, 40, 100, 20, I18n.get("gui.mountables2.chip.stay"), "stay", pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.STAY));
            mount.setFollowMode(Mountable.STAY);
        });
        final SwitchableWidget FOLLOW = new SwitchableWidget(0, 80, 100, 20, I18n.get("gui.mountables2.chip.follow"), "follow", pButton -> {
            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMountFollowTypePacket(entityId, Mountable.FOLLOW));
            mount.setFollowMode(Mountable.FOLLOW);
        });

        SwitchableWidget[] selectable = new SwitchableWidget[3];
        selectable[Mountable.FOLLOW] = FOLLOW;
        selectable[Mountable.WANDER] = WANDER;
        selectable[Mountable.STAY] = STAY;

        this.addRenderableWidget(new LinkedSwitchableWidget(posX / 2 - 50, posY / 2 - 100 , 100, 35 * selectable.length, mount.getFollowMode(), selectable));

        //TODO add function to arrow keys
        if (!mount.getLockSwitch()) {
            this.addRenderableWidget(new LinkedArrowWidget(posX / 2 - 50, posY / 2 + 40, 100, 16, arrowLeftModel, arrowRightModel, I18n.get("gui.mountables2.chip.model")));
        }

        this.addRenderableWidget(new LinkedArrowWidget(posX / 2 - 50, posY / 2 + (mount.getLockSwitch() ? 30 : 20), 100, 16, arrowLeftTex, arrowRightTex, I18n.get("gui.mountables2.chip.texture")));

        this.addRenderableWidget(new SwitchableWidget(posX / 2 - 50, (posY - 256) / 2 + 200, 100, 20, I18n.get("gui.mountables2.ai.ai"), "AI", pButton -> {
            minecraft.setScreen(new MountableAIScreen(entityId));
        }));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
