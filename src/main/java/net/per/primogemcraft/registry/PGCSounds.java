package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_PLACE = register("zipline_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_ATTACH = register("zipline_attach");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_DETACH = register("zipline_detach");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_SELECT = register("zipline_select");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_START = register("zipline_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_TRAVEL = register("zipline_travel");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_ARRIVE = register("zipline_arrive");

    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_ROLL = register("wish_roll");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_TEN = register("wish_ten");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_BLUE = register("wish_blue");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_PURPLE = register("wish_purple");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_GOLD = register("wish_gold");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAPTURING_RADIANCE = register("capturing_radiance");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_CORE_FEED = register("wish_core_feed");
    public static final DeferredHolder<SoundEvent, SoundEvent> CURIO_BROKEN = register("curio_broken");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHOICE_GRANT = register("choice_grant");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_PLANTED = register("bomb_planted");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_TICK = register("bomb_tick");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_FINAL_TICK = register("bomb_final_tick");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_DEFUSING = register("bomb_defusing");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_DEFUSED = register("bomb_defused");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENHANCEMENT_SUCCESS = register("enhancement_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> VARUNADA_LAZURITE_BUBBLE = register("varunada_lazurite_bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> VARUNADA_LAZURITE_BURST = register("varunada_lazurite_burst");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEAPON_CHARGE = register("weapon_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> THRUST_FLOURISH = register("thrust_flourish");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOIL_SHAPING = register("soil_shaping");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAM = register("scam");
    public static final DeferredHolder<SoundEvent, SoundEvent> A_DAY_OF_HOPE = register("a_day_of_hope");
    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGE_SURROUNDED_BY_GREEN = register("village_surrounded_by_green");
    public static final DeferredHolder<SoundEvent, SoundEvent> THE_VILLAGE_NO_LONGER_YOUNG = register("the_village_no_longer_young");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAKUSHINS_LULLABY = register("hakushins_lullaby");
    public static final DeferredHolder<SoundEvent, SoundEvent> BALLAD_OF_MANY_WATERS = register("ballad_of_many_waters");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASING_STARLIGHT_WITH_YOU = register("chasing_starlight_with_you");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPACE_WALK = register("space_walk");
    public static final DeferredHolder<SoundEvent, SoundEvent> SALTY_MOON = register("salty_moon");
    public static final DeferredHolder<SoundEvent, SoundEvent> TAKE_THE_JOURNEY = register("take_the_journey");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARCH_7TH_GIGGLE = register("march_7th_giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARCH_7TH_PUZZLED = register("march_7th_puzzled");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARCH_7TH_STARTLED = register("march_7th_startled");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPERIENCE_BOOK = register("experience_book");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOTTERY_DRAW = register("lottery_draw");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, name)));
    }
}
