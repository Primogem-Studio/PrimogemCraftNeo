package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class IntertwinedFatePickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(2000, 20.0F, 3, WeaponTier.NETHERITE_INCORRECT, PGCItems.INTERTWINED_FATE);

    private static final ResourceKey<LootTable> LOOT_TABLE =
            ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "blocks/intertwined_fate_pickaxe_loot"));
    private static final double LOOT_BASE = 0.3D;
    private static final double LOOT_STEP = 0.075D;
    private static final double COOLDOWN_BASE = 1.0D;
    private static final double COOLDOWN_STEP = 0.125D;
    private static final int COOLDOWN_TICKS_BASE = 20;
    private static final double COOLDOWN_TICKS_STEP = 2.5D;
    private static final float ATTACK_DAMAGE = 4.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String FLAVOR_TEXT = "flavor";
    private static final String LOOT_TEXT = "loot";
    private static final String COOLDOWN_TEXT = "cooldown";

    public IntertwinedFatePickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.note(FLAVOR_TEXT),
                WeaponDescription.of(PASSIVE_ACTION, LOOT_TEXT,
                        WishReports.percent(LOOT_BASE + LOOT_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(COOLDOWN_TEXT,
                        WishReports.number(COOLDOWN_BASE - COOLDOWN_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, net.minecraft.world.entity.LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerLevel server) || !(miningEntity instanceof Player player)) return result;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return result;
        if (!state.canOcclude()) return result;
        var refinement = WeaponState.of(stack).refinements();
        if (server.getRandom().nextDouble() >= LOOT_BASE + LOOT_STEP * (refinement - 1)) return result;
        var table = server.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        var params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, stack)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_STATE, state)
                .create(LootContextParamSets.BLOCK);
        for (var drop : table.getRandomItems(params)) player.drop(drop, false, false);
        player.getCooldowns().addCooldown(stack.getItem(),
                (int) Math.max(0.0D, COOLDOWN_TICKS_BASE - COOLDOWN_TICKS_STEP * (refinement - 1)));
        return result;
    }
}
