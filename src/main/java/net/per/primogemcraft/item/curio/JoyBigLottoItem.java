package net.per.primogemcraft.item.curio;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.per.primogemcraft.item.misc.StackOfCosmicBigLottoItem;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.LotteryCurioItem;

import java.util.List;

public class JoyBigLottoItem extends LotteryCurioItem {
    private static final double WIN_ODDS = 0.0D;

    private static final List<Shell> SHELLS = List.of(
            new Shell(3.0D, 1.0D, 0.0D, 5, List.of(
                    shell(FireworkExplosion.Shape.LARGE_BALL, 15882071, 3438841, false),
                    shell(FireworkExplosion.Shape.STAR, 16773394, 5701425, false))),
            new Shell(0.0D, 1.0D, 5.0D, 4, List.of(
                    shell(FireworkExplosion.Shape.BURST, 14873175, 3471673, true),
                    shell(FireworkExplosion.Shape.STAR, 16730642, 16009471, false))),
            new Shell(-4.0D, 1.0D, 0.0D, 4, List.of(
                    shell(FireworkExplosion.Shape.LARGE_BALL, 15095794, 16331918, true),
                    shell(FireworkExplosion.Shape.CREEPER, 2757375, 4784112, false))));

    public JoyBigLottoItem(Properties properties) {
        super(CurioForm.FUSION, WIN_ODDS, context -> {
        }, JoyBigLottoItem::celebrate, properties);
    }

    private static void celebrate(CurioContext context) {
        var player = context.player();
        var level = context.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.SCAM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        for (var shell : SHELLS)
            level.addFreshEntity(new FireworkRocketEntity(level, player.getX() + shell.x(), player.getY() + shell.y(), player.getZ() + shell.z(), rocket(shell)));
        context.give(StackOfCosmicBigLottoItem.rolled(context.random()));
    }

    private static ItemStack rocket(Shell shell) {
        var stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponents.FIREWORKS, new Fireworks(shell.flight(), shell.explosions()));
        return stack;
    }

    private static FireworkExplosion shell(FireworkExplosion.Shape shape, int color, int fadeColor, boolean trail) {
        return new FireworkExplosion(shape, IntList.of(color), IntList.of(fadeColor), trail, true);
    }

    private record Shell(double x, double y, double z, int flight, List<FireworkExplosion> explosions) {
    }
}
