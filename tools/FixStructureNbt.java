import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 将结构模板 NBT 中 1.21 的物品栈字段改为 1.20.1 格式。
 *
 * <p>1.21 用 {@code count}(int) + {@code components}；1.20.1 用 {@code Count}(byte) + {@code tag}。
 * 这里只处理定长字符串键的重命名与类型改写，不解析完整 NBT 树。
 *
 * <p>NBT 字符串键的编码为 TAG_String(8) + u2 长度 + UTF8 字节。
 * 定位到物品栈内的 "count" 键后，把键名改为 "Count"，并把随后的 TAG_Int(3) 改为 TAG_Byte(1)，
 * 同时把 4 字节 int 值压缩为 1 字节。
 */
public class FixStructureNbt {
    public static void main(String[] args) throws Exception {
        Path in = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        byte[] raw = read(in);
        boolean gz = raw.length > 2 && (raw[0] & 0xff) == 0x1f && (raw[1] & 0xff) == 0x8b;
        byte[] data = gz ? gunzip(raw) : raw;

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int fixed = 0;
        int i = 0;
        while (i < data.length) {
            // 复合标签内的键：值类型字节 + u2 键长 + 键名。
            // 1.21 的 count 为 TAG_Int(3)，1.20.1 的 Count 为 TAG_Byte(1)。
            if (i + 12 <= data.length && data[i] == 3
                    && u2(data, i + 1) == 5
                    && data[i + 3] == 'c' && data[i + 4] == 'o' && data[i + 5] == 'u'
                    && data[i + 6] == 'n' && data[i + 7] == 't') {
                int val = ((data[i + 8] & 0xff) << 24)
                        | ((data[i + 9] & 0xff) << 16)
                        | ((data[i + 10] & 0xff) << 8)
                        | (data[i + 11] & 0xff);
                bo.write(1);
                bo.write(0); bo.write(5);
                bo.write('C'); bo.write('o'); bo.write('u'); bo.write('n'); bo.write('t');
                bo.write(Math.max(0, Math.min(127, val)));
                i += 12;
                fixed++;
                continue;
            }
            bo.write(data[i]);
            i++;
        }

        byte[] result = bo.toByteArray();
        if (gz) {
            ByteArrayOutputStream g = new ByteArrayOutputStream();
            try (GZIPOutputStream go = new GZIPOutputStream(g)) { go.write(result); }
            result = g.toByteArray();
        }
        Files.write(out, result);
        System.out.println(in.getFileName() + ": fixed " + fixed + " count fields");
    }

    private static int u2(byte[] b, int p) {
        return ((b[p] & 0xff) << 8) | (b[p + 1] & 0xff);
    }

    private static byte[] read(Path p) throws IOException {
        return Files.readAllBytes(p);
    }

    private static byte[] gunzip(byte[] raw) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (GZIPInputStream gi = new GZIPInputStream(new ByteArrayInputStream(raw))) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = gi.read(buf)) > 0) bo.write(buf, 0, n);
        }
        return bo.toByteArray();
    }
}