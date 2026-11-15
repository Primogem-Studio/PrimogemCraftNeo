package net.per.primogemcraft.compat;

import net.minecraftforge.common.IExtensibleEnum;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.21 NeoForge {@code EnumProxy} 的兼容替代。
 *
 * <p>NeoForge 用 {@code EnumProxy} + {@code enumextensions.json} 在运行期给原版枚举
 * 追加常量。1.20.1 Forge 没有该机制，但被扩展的枚举实现了
 * {@link IExtensibleEnum}，并各自提供静态 {@code create(String, ...)} 工厂
 * （例如 {@code HumanoidModel.ArmPose.create(String, boolean, IArmPoseTransformer)}）。
 *
 * <p>本类通过反射调用该工厂来追加枚举常量，从而保留原有功能。
 * 若目标枚举没有可用工厂，则抛出明确异常而不是静默失效。
 */
public final class EnumProxy<T extends Enum<T>> {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final T value;

    @SafeVarargs
    public EnumProxy(Class<T> enumClass, Object... factoryArgs) {
        this.value = create(enumClass, factoryArgs);
    }

    /** 对应 1.21 的 {@code EnumProxy.getValue()}。 */
    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T create(Class<T> enumClass, Object[] factoryArgs) {
        if (!IExtensibleEnum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(
                    "Enum " + enumClass.getName() + " is not extensible on Forge 1.20.1");
        }
        String name = "primogemcraft_" + enumClass.getSimpleName().toLowerCase(java.util.Locale.ROOT)
                + "_" + COUNTER.incrementAndGet();
        // 工厂首参恒为常量名，其后是调用点给出的参数
        Object[] args = new Object[factoryArgs.length + 1];
        args[0] = name;
        System.arraycopy(factoryArgs, 0, args, 1, factoryArgs.length);

        for (Method method : enumClass.getDeclaredMethods()) {
            if (!method.getName().equals("create")) continue;
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != args.length) continue;
            if (!matches(params, args)) continue;
            try {
                method.setAccessible(true);
                return (T) method.invoke(null, args);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Failed to extend enum " + enumClass.getName(), e);
            }
        }
        throw new IllegalStateException("No matching create(...) factory on " + enumClass.getName()
                + " for " + args.length + " arguments");
    }

    private static boolean matches(Class<?>[] params, Object[] args) {
        for (int i = 0; i < params.length; i++) {
            var arg = args[i];
            if (arg == null) {
                if (params[i].isPrimitive()) return false;
                continue;
            }
            var expected = wrap(params[i]);
            if (!expected.isInstance(arg)) return false;
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
