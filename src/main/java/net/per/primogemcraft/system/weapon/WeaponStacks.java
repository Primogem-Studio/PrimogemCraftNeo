package net.per.primogemcraft.system.weapon;

public record WeaponStacks(int value, int capacity, boolean permanent) {
    public static final WeaponStacks NONE = new WeaponStacks(0, 0, false);

    public static WeaponStacks temporary(int value, int capacity) {
        return new WeaponStacks(value, capacity, false);
    }

    public static WeaponStacks permanent(int value, int capacity) {
        return new WeaponStacks(value, capacity, true);
    }

    public boolean visible() {
        return capacity > 0 && value > 0;
    }

    public boolean full() {
        return capacity > 0 && value >= capacity;
    }
}
