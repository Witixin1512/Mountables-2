package witixin.mountables2.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import witixin.mountables2.client.screen.widgets.LinkedSwitchableWidget;
import witixin.mountables2.client.screen.widgets.SwitchableWidget;
import witixin.mountables2.entity.movement.MountTravel;
import witixin.mountables2.entity.movement.MovementRegistry;
import witixin.mountables2.network.PacketHandler;
import witixin.mountables2.network.server.ServerUpdateMinorMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MountableAIScreen extends CommandChipScreen {

    String[] majors;
    byte totalRows;

    public MountableAIScreen(int entityId) {
        super(entityId);
    }

    @Override
    protected void init() {

        majors = new String[]{
                I18n.get("gui.mountables2.ai.fly"),
                I18n.get("gui.mountables2.ai.walk"),
                I18n.get("gui.mountables2.ai.swim")};

        totalRows = (byte) ((mount.canFly() ? 1 : 0) + (mount.canWalk() ? 1 : 0) + (mount.canSwim() ? 1 : 0));

        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();

        int row = 0; //button index used to calculate position
        int column = 0;//index of column use to calculate offset


        for (MountTravel.Major major : MountTravel.Major.values()) {
            //skip major if the corresponding movement is absent from mount
            if (!mount.canFly() && major == MountTravel.Major.FLY ||
                    !mount.canSwim() && major == MountTravel.Major.SWIM ||
                    !mount.canWalk() && major == MountTravel.Major.WALK)
                continue;

            List<SwitchableWidget> minors = new ArrayList<>();

            for (MountTravel travel : MovementRegistry.INSTANCE.getMajorMovement(major)) {

                final MountTravel.Minor mountMinor = mount.getMinorMovement(major);
                final boolean enabled = mountMinor.equals(travel.minor());

                SwitchableWidget widget = new SwitchableWidget(
                        0, 50 + (30 * row), //position
                        80, 20, //size
                        I18n.get(String.format("gui.mountables2.ai.%s", travel.minor().name().toLowerCase())),
                        pButton -> {
                            PacketHandler.INSTANCE.sendToServer(new ServerUpdateMinorMovement(entityId, travel.major(), travel.minor()));
                            this.mount.setMinorMovement(travel.major(), travel.minor());
                            pButton.setEnabled(true);
                        });
                widget.setEnabled(enabled);//enable widget if the set minor is the one from the widget
                minors.add(widget);
                row++;
            }
            row = 0;
            //add a linked widget with all buttons for the major of this iteration to the screen
            this.addRenderableWidget(new LinkedSwitchableWidget(posX / 2 - (46 * totalRows) + (100 * column), posY / 2 - 120, 80, 30 * minors.size(), minors.toArray(new SwitchableWidget[0])));
            column++;
        }

        addRenderableWidget(new SwitchableWidget((posX / 2 - 50), posY / 2 + 72, 100, 20, I18n.get("entity.mountables2.mountable_entity"),  pButton -> {
            minecraft.setScreen(new CommandChipScreen(entityId));
        }));
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int posX = minecraft.getWindow().getGuiScaledWidth();
        int posY = minecraft.getWindow().getGuiScaledHeight();
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        int i = 0;
        for (MountTravel.Major major : MountTravel.Major.values()) {
            if (canRenderMajor(major)) {
                String majorName = getStringFromMajor(major);
                Minecraft.getInstance().font.drawShadow(pPoseStack, majorName,   posX / 2f - Minecraft.getInstance().font.width(majorName) / 2f + 100 * i - getOffset(totalRows), (posY - 256) / 2f + 30, 0xffffff);
                ++i;
            }
        }
    }

    private boolean canRenderMajor(MountTravel.Major major) {
        if (major == MountTravel.Major.FLY) return this.mount.canFly();
        if (major == MountTravel.Major.WALK) return this.mount.canWalk();
        if (major == MountTravel.Major.SWIM) return this.mount.canSwim();
        return false;
    }

    private int getOffset(int totalRows) {
        if (totalRows == 1) return 7;
        if (totalRows == 2) return 50;
        if (totalRows == 3) return 100;
        return 0;
    }

    private String getStringFromMajor(MountTravel.Major major) {
        if (major == MountTravel.Major.FLY) return this.majors[0];
        if (major == MountTravel.Major.WALK) return this.majors[1];
        if (major == MountTravel.Major.SWIM) return this.majors[2];
        return "";
    }
}
