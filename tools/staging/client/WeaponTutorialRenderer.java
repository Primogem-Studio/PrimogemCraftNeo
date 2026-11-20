package net.per.primogemcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WeaponTutorialRenderer implements ClientTooltipComponent {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/weapon_tutorial.png");
    private static final int SOURCE = 64;
    private static final int BORDER = 8;
    private static final int INK = 0xFF65564D;
    private static final int ACCENT = 0xFFAE8051;
    private static final int RULE = 0xFFC5B6A7;
    private final ItemStack weapon;
    private final ItemStack forge = PGCItems.GORGEOUS_SMITHING_TABLE.get().getDefaultInstance();
    private final ItemStack superimposer = PGCItems.CUSTOM_SUPERIMPOSER.get().getDefaultInstance();
    private final ItemStack tablet = PGCItems.EQUILIBRIUM_TABLET.get().getDefaultInstance();
    private final List<ItemStack> ores = List.of(PGCItems.ENHANCEMENT_ORE.get().getDefaultInstance(),
            PGCItems.FINE_ENHANCEMENT_ORE.get().getDefaultInstance(), PGCItems.MYSTIC_ENHANCEMENT_ORE.get().getDefaultInstance());

    public record Content(ItemStack weapon) implements TooltipComponent {
    }

    public WeaponTutorialRenderer(Content content) {
        weapon = content.weapon();
    }

    @Override
    public int getHeight() {
        return 148;
    }

    @Override
    public int getWidth(Font font) {
        var width = 198;
        for (var ore : ores) width = Math.max(width, 24 + 3 * (font.width(experience(ore)) + 20));
        width = Math.max(width, 104 + font.width(label("cap", PGCConfig.WEAPON_MAX_LEVEL_BONUS.get())));
        width = Math.max(width, 160 + font.width(label("level")));
        return Math.max(width, 112 + font.width(label("attack")));
    }

    @Override
    public void renderImage(Font font, int x, int y, DrawContext graphics) {
        var width = getWidth(font);
        background(graphics, x, y + 2, width, 142);
        graphics.fill(x + 9, y + 60, x + width - 9, y + 61, RULE);
        graphics.fill(x + 9, y + 108, x + width - 9, y + 109, RULE);

        graphics.renderItem(weapon, x + 12, y + 12);
        plus(graphics, x + 34, y + 20);
        graphics.renderItem(ores.get(2), x + 44, y + 12);
        arrow(graphics, x + 66, y + 20);
        graphics.renderItem(forge, x + 84, y + 12);
        arrow(graphics, x + 106, y + 20);
        graphics.renderItem(weapon, x + 124, y + 12);
        graphics.drawString(font, label("level"), x + 148, y + 16, INK, false);
        var column = (width - 24) / 3;
        for (var index = 0; index < ores.size(); index++) {
            var left = x + 12 + index * column;
            var ore = ores.get(index);
            graphics.renderItem(ore, left, y + 37);
            graphics.drawString(font, experience(ore), left + 18, y + 41, INK, false);
        }

        graphics.renderItem(weapon, x + 12, y + 66);
        plus(graphics, x + 34, y + 74);
        graphics.renderItem(superimposer, x + 44, y + 66);
        for (var step = 0; step < 7; step++)
            graphics.fill(x + 69 - step / 2, y + 70 + step, x + 70 - step / 2, y + 71 + step, INK);
        graphics.renderItem(weapon, x + 73, y + 66);
        arrow(graphics, x + 96, y + 74);
        graphics.renderItem(forge, x + 114, y + 66);
        arrow(graphics, x + 136, y + 74);
        StarRow.render(graphics, x + 155, y + 70, 1, 0);
        plus(graphics, x + 173, y + 74);
        StarRow.render(graphics, x + 12, y + 94, WeaponEnhancement.MAX_REFINEMENT, 0);
        arrow(graphics, x + 65, y + 97);
        graphics.drawString(font, label("cap", PGCConfig.WEAPON_MAX_LEVEL_BONUS.get()), x + 86, y + 93, INK, false);

        graphics.renderItem(tablet, x + 12, y + 118);
        mouse(graphics, x + 34, y + 119);
        arrow(graphics, x + 51, y + 126);
        graphics.renderItem(weapon, x + 69, y + 118);
        graphics.drawString(font, label("attack"), x + 100, y + 122, INK, false);
    }

    private static void background(DrawContext graphics, int x, int y, int width, int height) {
        var face = SOURCE - BORDER * 2;
        var centerW = Math.max(0, width - BORDER * 2);
        var centerH = Math.max(0, height - BORDER * 2);
        var rightX = x + width - BORDER;
        var bottomY = y + height - BORDER;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (centerH > 0) {
            graphics.blit(BACKGROUND, x, y + BORDER, BORDER, centerH, 0.0F, BORDER, BORDER, face, SOURCE, SOURCE);
            graphics.blit(BACKGROUND, rightX, y + BORDER, BORDER, centerH, SOURCE - BORDER, BORDER, BORDER, face, SOURCE, SOURCE);
        }
        if (centerW > 0) {
            graphics.blit(BACKGROUND, x + BORDER, y, centerW, BORDER, BORDER, 0.0F, face, BORDER, SOURCE, SOURCE);
            graphics.blit(BACKGROUND, x + BORDER, bottomY, centerW, BORDER, BORDER, SOURCE - BORDER, face, BORDER, SOURCE, SOURCE);
        }
        if (centerW > 0 && centerH > 0)
            graphics.blit(BACKGROUND, x + BORDER, y + BORDER, centerW, centerH, BORDER, BORDER, face, face, SOURCE, SOURCE);
        graphics.blit(BACKGROUND, x, y, BORDER, BORDER, 0.0F, 0.0F, BORDER, BORDER, SOURCE, SOURCE);
        graphics.blit(BACKGROUND, rightX, y, BORDER, BORDER, SOURCE - BORDER, 0.0F, BORDER, BORDER, SOURCE, SOURCE);
        graphics.blit(BACKGROUND, x, bottomY, BORDER, BORDER, 0.0F, SOURCE - BORDER, BORDER, BORDER, SOURCE, SOURCE);
        graphics.blit(BACKGROUND, rightX, bottomY, BORDER, BORDER, SOURCE - BORDER, SOURCE - BORDER, BORDER, BORDER, SOURCE, SOURCE);
    }

    private static Text label(String suffix, Object... arguments) {
        return Text.translatable("weapon.primogemcraft.guide." + suffix, arguments);
    }

    private static Text experience(ItemStack ore) {
        return label("xp", WeaponEnhancement.xpPerOre(ore.getItem()));
    }

    private static void plus(DrawContext graphics, int x, int y) {
        graphics.fill(x - 3, y, x + 4, y + 1, ACCENT);
        graphics.fill(x, y - 3, x + 1, y + 4, ACCENT);
    }

    private static void arrow(DrawContext graphics, int x, int y) {
        graphics.fill(x, y, x + 10, y + 1, ACCENT);
        for (var offset = 1; offset <= 3; offset++) {
            graphics.fill(x + 9 - offset, y - offset, x + 10 - offset, y - offset + 1, ACCENT);
            graphics.fill(x + 9 - offset, y + offset, x + 10 - offset, y + offset + 1, ACCENT);
        }
    }

    private static void mouse(DrawContext graphics, int x, int y) {
        graphics.fill(x + 2, y, x + 9, y + 14, INK);
        graphics.fill(x, y + 2, x + 11, y + 12, INK);
        graphics.fill(x + 1, y + 3, x + 10, y + 11, 0xFFE9E3D9);
        graphics.fill(x + 3, y + 1, x + 8, y + 13, 0xFFE9E3D9);
        graphics.fill(x + 6, y + 2, x + 9, y + 7, ACCENT);
        graphics.fill(x + 5, y + 1, x + 6, y + 8, INK);
    }
}
