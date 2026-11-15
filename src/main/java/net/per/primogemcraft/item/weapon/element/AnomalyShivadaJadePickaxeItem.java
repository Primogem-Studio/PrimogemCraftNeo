package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AnomalyShivadaJadePickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(2561, 8.0F, 5, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.ELEMENTAL_CRYSTAL_DUST, PGCItems.SHIVADA_JADE_SLIVER);

    private static final TagKey<Block> CRYSTAL_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(MOD_ID, "elemental_crystal_blocks"));
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int COOLDOWN = 20;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final int BREAK_EVENT = 2001;
    private static final String RIGHT_CLICK_BLOCK = "right_click_block";
    private static final String TRANSFORM_TEXT = "transform";
    private static final String DESTROY_TEXT = "destroy";
    private static final String WAX_SEAL_TEXT = "wax_seal";

    public AnomalyShivadaJadePickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(WAX_SEAL_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK_BLOCK, TRANSFORM_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), Formatting.AQUA),
                WishReports.percent(sealed ? 1.0D : 0.95D, Formatting.AQUA)));
        descriptions.add(WeaponDescription.note(DESTROY_TEXT));
        return List.copyOf(descriptions);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null) return result;
        var pos = context.getBlockPos();
        var state = server.getBlockState(pos);
        if (!state.isIn(CRYSTAL_BLOCKS)) return result;
        var sealed = Element.holdsWaxSeal(player, Element.CRYO);
        var stack = context.getStack();
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        if (server.getRandom().nextDouble() < (sealed ? 1.0D : 0.95D)) {
            server.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
            server.setBlockState(pos, randomCrystalBlock(server).getDefaultState(), Block.NOTIFY_ALL);
            return ActionResult.SUCCESS;
        }
        server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        server.syncWorldEvent(BREAK_EVENT, pos, Block.getRawIdFromState(state));
        return ActionResult.SUCCESS;
    }

    private static Block randomCrystalBlock(ServerWorld level) {
        var holders = level.getRegistryManager().get(RegistryKeys.BLOCK).getEntryList(CRYSTAL_BLOCKS);
        if (holders.isEmpty() || holders.get().size() == 0) return Blocks.AIR;
        return holders.get().getRandom(level.getRandom()).map(RegistryEntry::value).orElse(Blocks.AIR);
    }
}
