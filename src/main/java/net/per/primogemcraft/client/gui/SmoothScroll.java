package net.per.primogemcraft.client.gui;

import net.minecraft.util.Mth;

public final class SmoothScroll {
    private static final float RESPONSE = 26.0F;
    private static final float SETTLE = 0.002F;

    private float limit;
    private float target;
    private float shown;

    public void setLimit(float limit) {
        this.limit = Math.max(0.0F, limit);
        target = Mth.clamp(target, 0.0F, this.limit);
        shown = Mth.clamp(shown, 0.0F, this.limit);
    }

    public void scroll(double notches) {
        if (limit <= 0.0F) return;
        target = Mth.clamp((float) (target - notches), 0.0F, limit);
    }

    public void jumpTo(float progress) {
        target = Mth.clamp(progress, 0.0F, 1.0F) * limit;
    }

    public void advance(float frameSeconds) {
        shown = Mth.lerp(1.0F - (float) Math.exp(-RESPONSE * frameSeconds), shown, target);
        if (Math.abs(target - shown) < SETTLE) shown = target;
    }

    public float shown() {
        return shown;
    }

    public float progress() {
        return limit <= 0.0F ? 0.0F : Mth.clamp(shown / limit, 0.0F, 1.0F);
    }
}
