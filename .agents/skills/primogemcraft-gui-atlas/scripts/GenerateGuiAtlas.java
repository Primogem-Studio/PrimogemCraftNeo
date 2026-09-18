import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class GenerateGuiAtlas {
    private static final int CELL = 32;
    private static final int COLUMNS = 2;
    private static final int ROWS = 2;

    private static final int SLOT_SIZE = 18;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 20;

    private static final int PANEL_BORDER = 0xFF000000;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_BODY = 0xFFC6C6C6;
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_BODY = 0xFF8B8B8B;
    private static final int TRACK_BORDER = 0xFF373737;
    private static final int TRACK_BODY = 0xFF2E2E2E;
    private static final int THUMB_BORDER = 0xFF000000;
    private static final int THUMB_LIGHT = 0xFFFFFFFF;
    private static final int THUMB_SHADOW = 0xFF555555;
    private static final int THUMB_BODY = 0xFFC6C6C6;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("usage: GenerateGuiAtlas <output.png>");
        var image = new BufferedImage(COLUMNS * CELL, ROWS * CELL, BufferedImage.TYPE_INT_ARGB);
        panel(image, 0, 0, 16, 16);
        slot(image, CELL, 0);
        track(image, 0, CELL);
        thumb(image, CELL, CELL);
        ImageIO.write(image, "png", new File(args[0]));
    }

    private static void panel(BufferedImage image, int originX, int originY, int width, int height) {
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var color = PANEL_BODY;
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) color = PANEL_BORDER;
                else if (x == 1 && y == 1) color = PANEL_LIGHT;
                else if (x == width - 2 && y == height - 2) color = PANEL_SHADOW;
                else if (x == 1 || y == 1) color = PANEL_LIGHT;
                else if (x == width - 2 || y == height - 2) color = PANEL_SHADOW;
                image.setRGB(originX + x, originY + y, color);
            }
        }
    }

    private static void slot(BufferedImage image, int originX, int originY) {
        for (var y = 0; y < SLOT_SIZE; y++) {
            for (var x = 0; x < SLOT_SIZE; x++) {
                var color = SLOT_BODY;
                if (x == 0 || y == 0) color = SLOT_BORDER;
                else if (x == SLOT_SIZE - 1 || y == SLOT_SIZE - 1) color = SLOT_LIGHT;
                image.setRGB(originX + x, originY + y, color);
            }
        }
    }

    private static void track(BufferedImage image, int originX, int originY) {
        for (var y = 0; y < BAR_HEIGHT; y++) {
            for (var x = 0; x < BAR_WIDTH; x++) {
                var edge = x == 0 || x == BAR_WIDTH - 1;
                image.setRGB(originX + x, originY + y, edge ? TRACK_BORDER : TRACK_BODY);
            }
        }
    }

    private static void thumb(BufferedImage image, int originX, int originY) {
        for (var y = 0; y < BAR_HEIGHT; y++) {
            for (var x = 0; x < BAR_WIDTH; x++) {
                var color = THUMB_BODY;
                if (x == 0 || x == BAR_WIDTH - 1 || y == 0 || y == BAR_HEIGHT - 1) color = THUMB_BORDER;
                else if (x == 1) color = THUMB_LIGHT;
                else if (x == BAR_WIDTH - 2) color = THUMB_SHADOW;
                image.setRGB(originX + x, originY + y, color);
            }
        }
    }
}
