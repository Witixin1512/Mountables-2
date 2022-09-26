package witixin.mountables2.entity.newmountable.movement;

import net.minecraft.network.FriendlyByteBuf;

public record KeyStrokeMovement(boolean up, boolean down, boolean left, boolean right, boolean jump) {
    public static final KeyStrokeMovement NONE = new KeyStrokeMovement(false, false, false, false, false);

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(up);
        buf.writeBoolean(down);
        buf.writeBoolean(left);
        buf.writeBoolean(right);
        buf.writeBoolean(jump);
    }

    public boolean isPurelyLateral() {
        return (left && !right) || (!left && right);
    }

    public boolean isPurelyFrontal() {
        return (up && !down) || (!up && down);
    }

    public boolean isLateral() {
        return left || right;
    }

    public boolean isFrontal() {
        return up || down;
    }

    public static KeyStrokeMovement decode(FriendlyByteBuf buf) {
        return new KeyStrokeMovement(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public String toString() {
        return "KeyStrokeMovement{" +
                "up=" + up +
                ", down=" + down +
                ", left=" + left +
                ", right=" + right +
                ", jump=" + jump +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KeyStrokeMovement ksm && ksm.up == up && ksm.down == down && ksm.left == left && ksm.right == right & ksm.jump == jump;
    }
}
