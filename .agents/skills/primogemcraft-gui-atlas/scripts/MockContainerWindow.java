import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Draws a container window offline, straight from the sprite sheet, so slot alignment, panel padding and the scrollbar
 * can be checked without launching Minecraft. The constants below mirror {@code ContainerWindowLayout} and
 * {@code GuiAtlas}; when either changes, change them here too, otherwise this check verifies the wrong geometry.
 */
public final class MockContainerWindow {
    private static final int SLOT = 18;
    private static final int COLUMNS = 9;
    private static final int VISIBLE_ROWS = 6;
    private static final int TOTAL_ROWS = 16;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 18;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 6;
    private static final int PANEL_PADDING = 7;
    private static final int INVENTORY_GAP = 14;
    private static final int PANEL_INSET = 2;

    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;
    private static final int PANEL_SIZE = 16;
    private static final int SLOT_U = 32;
    private static final int SLOT_V = 0;
    private static final int TRACK_U = 0;
    private static final int TRACK_V = 32;
    private static final int TRACK_W = 6;
    private static final int TRACK_H = 20;
    private static final int THUMB_U = 32;
    private static final int THUMB_V = 32;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("usage: MockContainerWindow <atlas.png> <output.png>");
        var atlas = ImageIO.read(new File(args[0]));
        var scrollable = TOTAL_ROWS > VISIBLE_ROWS;
        var gridTop = GRID_Y - 1;
        var gridHeight = VISIBLE_ROWS * SLOT;
        var scrollbarX = GRID_X - 1 + COLUMNS * SLOT + SCROLLBAR_GAP;
        var inventoryY = GRID_Y + VISIBLE_ROWS * SLOT + INVENTORY_GAP;
        var hotbarY = inventoryY + 3 * SLOT + 4;
        var width = (scrollable ? scrollbarX + SCROLLBAR_WIDTH : GRID_X - 1 + COLUMNS * SLOT) + PANEL_PADDING;
        var height = hotbarY + SLOT - 1 + PANEL_PADDING;
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        panel(image, atlas, width, height);
        for (var row = 0; row < VISIBLE_ROWS; row++)
            for (var column = 0; column < COLUMNS; column++)
                blit(image, atlas, SLOT_U, SLOT_V, SLOT, SLOT, GRID_X + column * SLOT - 1, GRID_Y + row * SLOT - 1);
        if (scrollable) {
            stretched(image, atlas, TRACK_U, TRACK_V, TRACK_W, TRACK_H, scrollbarX, gridTop, SCROLLBAR_WIDTH, gridHeight);
            var thumb = Math.max(TRACK_H, Math.round(gridHeight * (VISIBLE_ROWS / (float) TOTAL_ROWS)));
            stretched(image, atlas, THUMB_U, THUMB_V, TRACK_W, TRACK_H, scrollbarX, gridTop + 30, SCROLLBAR_WIDTH, thumb);
        }
        for (var row = 0; row < 3; row++)
            for (var column = 0; column < COLUMNS; column++)
                blit(image, atlas, SLOT_U, SLOT_V, SLOT, SLOT, GRID_X + column * SLOT - 1, inventoryY + row * SLOT - 1);
        for (var column = 0; column < COLUMNS; column++)
            blit(image, atlas, SLOT_U, SLOT_V, SLOT, SLOT, GRID_X + column * SLOT - 1, hotbarY - 1);
        label(image, GRID_X, 6);
        label(image, GRID_X, inventoryY - 11);
        ImageIO.write(image, "png", new File(args[1]));
        System.out.println("window " + width + "x" + height + ", grid frame " + gridTop + ".." + (gridTop + gridHeight)
                + ", scrollbar x " + scrollbarX + " w " + SCROLLBAR_WIDTH + ", inventory y " + inventoryY
                + ", hotbar y " + hotbarY);
    }

    private static void label(BufferedImage image, int x, int y) {
        for (var py = 0; py < 9; py++)
            for (var px = 0; px < 40; px++)
                image.setRGB(x + px, y + py, 0xFF3F3F3F);
    }

    private static void panel(BufferedImage image, BufferedImage atlas, int width, int height) {
        var inset = PANEL_INSET;
        var face = PANEL_SIZE - inset * 2;
        blit(image, atlas, PANEL_U, PANEL_V, inset, inset, 0, 0);
        blit(image, atlas, PANEL_U + PANEL_SIZE - inset, PANEL_V, inset, inset, width - inset, 0);
        blit(image, atlas, PANEL_U, PANEL_V + PANEL_SIZE - inset, inset, inset, 0, height - inset);
        blit(image, atlas, PANEL_U + PANEL_SIZE - inset, PANEL_V + PANEL_SIZE - inset, inset, inset, width - inset, height - inset);
        stretched(image, atlas, PANEL_U + inset, PANEL_V, face, inset, inset, 0, width - inset * 2, inset);
        stretched(image, atlas, PANEL_U + inset, PANEL_V + PANEL_SIZE - inset, face, inset, inset, height - inset, width - inset * 2, inset);
        stretched(image, atlas, PANEL_U, PANEL_V + inset, inset, face, 0, inset, inset, height - inset * 2);
        stretched(image, atlas, PANEL_U + PANEL_SIZE - inset, PANEL_V + inset, inset, face, width - inset, inset, inset, height - inset * 2);
        stretched(image, atlas, PANEL_U + inset, PANEL_V + inset, face, face, inset, inset, width - inset * 2, height - inset * 2);
    }

    private static void blit(BufferedImage image, BufferedImage atlas, int u, int v, int w, int h, int x, int y) {
        for (var py = 0; py < h; py++)
            for (var px = 0; px < w; px++)
                image.setRGB(x + px, y + py, atlas.getRGB(u + px, v + py));
    }

    private static void stretched(BufferedImage image, BufferedImage atlas, int u, int v, int sourceWidth, int sourceHeight,
                                  int x, int y, int width, int height) {
        for (var py = 0; py < height; py++)
            for (var px = 0; px < width; px++)
                image.setRGB(x + px, y + py, atlas.getRGB(u + px * sourceWidth / width, v + py * sourceHeight / height));
    }
}
