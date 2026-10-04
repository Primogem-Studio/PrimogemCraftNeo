package net.per.primogemcraft.client.gui.teyvat;

/** Shares the collaboration list geometry between rendering, buttons and scrollbar input. */
public record KatheryneListLayout(int top, int visibleRows, int height) {
    public static final KatheryneListLayout SHOP = new KatheryneListLayout(51, 5, 119);
    public static final KatheryneListLayout EXCHANGE = new KatheryneListLayout(96, 3, 74);

    public int maxScroll(int size) {
        return Math.max(0, size - visibleRows);
    }

    public int clampScroll(int scroll, int size) {
        return Math.clamp(scroll, 0, maxScroll(size));
    }

    public int rowTop(int row) {
        return top + row * 24;
    }

    public int scrollAt(double mouseY, int size) {
        var maximum = maxScroll(size);
        if (maximum == 0) return 0;
        var thumb = Math.max(12, height * visibleRows / size);
        var position = Math.clamp((mouseY - top - thumb / 2.0) / (height - thumb), 0.0, 1.0);
        return (int) Math.round(position * maximum);
    }
}
