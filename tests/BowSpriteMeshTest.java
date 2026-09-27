import net.per.primogemcraft.client.BowSpriteMesh;

import javax.imageio.ImageIO;
import java.nio.file.Path;

public final class BowSpriteMeshTest {
    public static void main(String[] args) throws Exception {
        check(BowSpriteMesh.build(1, 0, 1, pixel -> true).size() == 6);
        check(BowSpriteMesh.build(2, 0, 1, pixel -> true).size() == 10);
        check(BowSpriteMesh.build(2, 0, 1, pixel -> pixel < 2).size() == 8);
        check(BowSpriteMesh.build(3, 0, 1, pixel -> pixel != 4).size() == 18);
        check(BowSpriteMesh.build(2, 0, 1, pixel -> false).size() == 2);
        var image = ImageIO.read(Path.of("src/main/resources/assets/primogemcraft/textures/item/example_wish_bow.png").toFile());
        var size = image.getWidth();
        var count = image.getHeight() / size;
        check(count == 5);
        for (var frame = 0; frame < count; frame++) {
            var offset = frame * size;
            var mesh = BowSpriteMesh.build(size, frame, count, pixel -> (image.getRGB(pixel % size, offset + pixel / size) >>> 24) != 0);
            check(mesh.size() > 6);
            for (var quad : mesh) {
                var a = quad.vertices().get(0);
                var b = quad.vertices().get(1);
                var c = quad.vertices().get(2);
                var normalX = (b.y() - a.y()) * (c.z() - a.z()) - (b.z() - a.z()) * (c.y() - a.y());
                var normalY = (b.z() - a.z()) * (c.x() - a.x()) - (b.x() - a.x()) * (c.z() - a.z());
                var normalZ = (b.x() - a.x()) * (c.y() - a.y()) - (b.y() - a.y()) * (c.x() - a.x());
                check(normalX * quad.normalX() + normalY * quad.normalY() + normalZ * quad.normalZ() > 0);
                for (var vertex : quad.vertices()) {
                    check(vertex.x() >= 0 && vertex.x() <= 1 && vertex.y() >= 0 && vertex.y() <= 1);
                    check(vertex.z() == 7.5F / 16.0F || vertex.z() == 8.5F / 16.0F);
                    check(vertex.u() >= 0 && vertex.u() <= 1);
                    check(vertex.v() >= frame / (float) count && vertex.v() <= (frame + 1.0F) / count);
                }
            }
        }
        System.out.println("Bow sprite thickness, silhouette, normals and frame UV checks passed");
    }

    private static void check(boolean valid) {
        if (!valid) throw new AssertionError();
    }
}
