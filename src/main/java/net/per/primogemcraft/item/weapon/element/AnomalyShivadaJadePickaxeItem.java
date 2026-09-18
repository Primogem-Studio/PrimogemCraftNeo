package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    private static final Tier TIER = new WeaponTier(2561, 8.0F, 5, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.ELEMENTAL_CRYSTAL_DUST, PGCItems.SHIVADA_JADE_SLIVER);

    private static final TagKey<Block> CRYSTAL_BLOCKS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "elemental_crystal_blocks"));
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

    public AnomalyShivadaJadePickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(WAX_SEAL_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK_BLOCK, TRANSFORM_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), ChatFormatting.AQUA),
                WishReports.percent(sealed ? 1.0D : 0.95D, ChatFormatting.AQUA)));
        descriptions.add(WeaponDescription.note(DESTROY_TEXT));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null) return result;
        var pos = context.getClickedPos();
        var state = server.getBlockState(pos);
        if (!state.is(CRYSTAL_BLOCKS)) return result;
        var sealed = Element.holdsWaxSeal(player, Element.CRYO);
        var stack = context.getItemInHand();
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        if (server.getRandom().nextDouble() < (sealed ? 1.0D : 0.95D)) {
            server.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
            server.setBlock(pos, randomCrystalBlock(server).defaultBlockState(), Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        server.levelEvent(BREAK_EVENT, pos, Block.getId(state));
        return InteractionResult.SUCCESS;
    }

    private static Block randomCrystalBlock(ServerLevel level) {
        var holders = level.registryAccess().registryOrThrow(Registries.BLOCK).getTag(CRYSTAL_BLOCKS);
        if (holders.isEmpty() || holders.get().size() == 0) return Blocks.AIR;
        return holders.get().getRandomElement(level.getRandom()).map(Holder::value).orElse(Blocks.AIR);
    }
}
