package witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.movement.MountTravel;
import witixin.mountables2.entity.movement.MovementRegistry;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerUpdateMinorMovement;

import java.util.ArrayList;
import java.util.List;

public class MountableAIScreen extends CommandChipScreen {

    public MountableAIScreen(int entityId) {
        super(entityId);
    }

    @Override
    protected void init() {
        super.init();
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        int row = 0; //button index used to calculate position
        int column = 0;//index of column use to calculate offset
        byte totalRows = (byte) ((mount.canFly() ? 1 : 0) + (mount.canWalk() ? 1 : 0) + (mount.canSwim() ? 1 : 0));

        for (MountTravel.Major major : MountTravel.Major.values()) {
            //skip major if the corresponding movement is absent from mount
            if (!mount.canFly() && major == MountTravel.Major.FLY ||
                    !mount.canSwim() && major == MountTravel.Major.SWIM ||
                    !mount.canWalk() && major == MountTravel.Major.WALK)
                continue;

            List<SwitchableWidget> minors = new ArrayList<>();

            for (MountTravel travel : MovementRegistry.INSTANCE.getMajorMovement(major)) {
                SwitchableWidget widget = new SwitchableWidget(
                        0, 50 + (30 * row), //position
                        80, 20, //size
                        I18n.get(String.format("gui.mountables2.ai.%s", travel.minor().name().toLowerCase())),
                        travel.minor().name(),
                        pButton -> {
                            this.mount.setMinorMovement(travel.major(), travel.minor());
                            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMinorMovement(entityId, travel.major(), travel.minor()));
                        });
                widget.setEnabled(mount.getMinorMovement(major).equals(travel.minor()));//enable widget if the set minor is the one from the widget
                minors.add(widget);
                row++;
            }
            row = 0;
            //add a linked widget with all buttons for the major of this iteration to the screne
            this.addRenderableWidget(new LinkedSwitchableWidget(posX / 2 - (46 * totalRows) + (100 * column), 0, 80, 30 * minors.size(), (byte) 0, minors.toArray(new SwitchableWidget[0])));
            column++;
        }

        addRenderableWidget(new SwitchableWidget((posX / 2 - 50), posY / 2 + 72, 100, 20, I18n.get("entity.mountables2.mountable_entity"), "Mountable", pButton -> {
            minecraft.setScreen(new CommandChipScreen(entityId));
        }));
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        String[] majors = new String[]{
                I18n.get("gui.mountables2.ai.swim"),
                I18n.get("gui.mountables2.ai.walk"),
                I18n.get("gui.mountables2.ai.fly")
        };
        int i = 0;
        for (String major : majors) {
            Minecraft.getInstance().font.drawShadow(pPoseStack, major, -100 + (100 * i) + posX / 2f - Minecraft.getInstance().font.width(major) / 2f, (posY - 256) / 2f + 30, 0xffffff);
            i++;
        }
    }
}
