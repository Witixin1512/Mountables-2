package witixin.mountables2.entity.movement;

import net.minecraft.network.FriendlyByteBuf;

public record KeyStrokeMovement(boolean forwards, boolean backwards, boolean left, boolean right, boolean spacebar) {
    public static final KeyStrokeMovement NONE = new KeyStrokeMovement(false, false, false, false, false);

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(forwards);
        buf.writeBoolean(backwards);
        buf.writeBoolean(left);
        buf.writeBoolean(right);
        buf.writeBoolean(spacebar);
    }

    public static KeyStrokeMovement decode(FriendlyByteBuf buf) {
        return new KeyStrokeMovement(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public boolean isPurelyLateral() {
        return left ^ right;
    }

    public boolean isPurelyFrontal() {
        return forwards ^ backwards;
    }

    public boolean isLateral() {
        return left || right;
    }

    public boolean isFrontal() {
        return forwards || backwards;
    }   

    @Override
    public String toString() {
        return "KeyStrokeMovement{" +
                "forwards=" + forwards +
                ", backwards=" + backwards +
                ", left=" + left +
                ", right=" + right +
                ", jump=" + spacebar +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KeyStrokeMovement ksm && ksm.forwards == forwards && ksm.left == left && ksm.right == right & ksm.spacebar == spacebar && ksm.backwards == backwards;
    }
}
