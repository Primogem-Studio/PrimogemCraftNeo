package net.hacker.genshincraft.element;

/**
 * GenshinCraft API stub for compilation. At runtime the real GenshinCraft mod provides this class.
 */
public class Element {
    public enum Type {
        Anemo, Geo, Electro, Dendro, Hydro, Pyro, Cryo
    }

    private final Type type;
    private final float quantity;
    private final float strength;

    public Element(Type type, float quantity, float strength) {
        this.type = type;
        this.quantity = quantity;
        this.strength = strength;
    }

    public Type getType() { return type; }
    public float getQuantity() { return quantity; }
    public float getStrength() { return strength; }

    public static Element fromType(Type type, float quantity, float strength) {
        return new Element(type, quantity, strength);
    }
}
