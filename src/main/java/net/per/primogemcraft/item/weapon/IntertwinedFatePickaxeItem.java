package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class IntertwinedFatePickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(2000, 20.0F, 3, WeaponTier.NETHERITE_INCORRECT, PGCItems.INTERTWINED_FATE);

    private static final Identifier LOOT_TABLE =
            Identifier.of(MOD_ID, "blocks/intertwined_fate_pickaxe_loot");
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

    public IntertwinedFatePickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), net.minecraft.registry.tag.BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.note(FLAVOR_TEXT),
                WeaponDescription.of(PASSIVE_ACTION, LOOT_TEXT,
                        WishReports.percent(LOOT_BASE + LOOT_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(COOLDOWN_TEXT,
                        WishReports.number(COOLDOWN_BASE - COOLDOWN_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, net.minecraft.entity.LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
        if (!state.isOpaque()) return result;
        var refinement = WeaponState.of(stack).refinements();
        if (server.getRandom().nextDouble() >= LOOT_BASE + LOOT_STEP * (refinement - 1)) return result;
        var table = server.getServer().getLootManager().getLootTable(LOOT_TABLE);
        var params = new LootContextParameterSet.Builder(server)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .add(LootContextParameters.TOOL, stack)
                .addOptional(LootContextParameters.THIS_ENTITY, player)
                .addOptional(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);
        for (var drop : table.generateLoot(params)) player.dropItem(drop, false, false);
        player.getItemCooldownManager().set(stack.getItem(),
                (int) Math.max(0.0D, COOLDOWN_TICKS_BASE - COOLDOWN_TICKS_STEP * (refinement - 1)));
        return result;
    }
}
