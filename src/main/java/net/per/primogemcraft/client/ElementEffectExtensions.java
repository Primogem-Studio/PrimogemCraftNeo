package net.per.primogemcraft.client;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.per.primogemcraft.registry.PGCEffects;

import java.util.function.Consumer;

/**
 * 闅愯棌閮ㄥ垎鏁堟灉鐨?HUD/鐗╁搧鏍忓浘鏍囥€? *
 * <p>1.21 鐢?{@code RegisterClientExtensionsEvent.registerMobEffect}锛?.20.1 娌℃湁璇ヤ簨浠讹紝
 * 鏀逛负瑕嗗啓 {@link MobEffect#initializeClient(Consumer)}鈥斺€旇繖鏄?Forge 1.20.1 鎻愪緵鐨? * 瀹樻柟鎵╁睍鍏ュ彛锛屾晥鏋滅瓑浠枫€傜敱 {@code PGCEffects} 鍦ㄦ敞鍐屽悇鏁堟灉鏃惰皟鐢?{@link #hide}銆? */
public final class ElementEffectExtensions {
    private static final IClientMobEffectExtensions HIDDEN = new IClientMobEffectExtensions() {
        @Override
        public boolean isVisibleInGui(MobEffectInstance effect) {
            return false;
        }

        @Override
        public boolean isVisibleInInventory(MobEffectInstance effect) {
            return false;
        }
    };

    private ElementEffectExtensions() {
    }

    /** 渚涙晥鏋滅被鍦?initializeClient 涓皟鐢紝闅愯棌鍏跺浘鏍囥€?*/
    public static void hide(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(HIDDEN);
    }

    /** 闇€瑕侀殣钘忓浘鏍囩殑鍏ㄩ儴鏁堟灉锛屼緵娉ㄥ唽渚х粺涓€搴旂敤銆?*/
    public static MobEffect[] hiddenEffects() {
        return new MobEffect[]{
                PGCEffects.FLIGHT.get(),
                PGCEffects.DAMAGE_ABSORPTION_COOLDOWN.get(),
                PGCEffects.RETALIATION_COOLDOWN.get(),
                PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT.get(),
                PGCEffects.BURNING_RETALIATION.get(),
                PGCEffects.BURNING_RECOVERY.get(),
                PGCEffects.BUBBLE_LIMIT.get()
        };
    }
}