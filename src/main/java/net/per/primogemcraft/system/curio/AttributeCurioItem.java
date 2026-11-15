package net.per.primogemcraft.system.curio;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public class AttributeCurioItem extends CurioItem {
    private final List<Modifier> modifiers;

    public AttributeCurioItem(CurioForm form, List<Modifier> modifiers, Settings properties) {
        super(CurioTrigger.ACTIVE, form, properties);
        this.modifiers = modifiers;
    }

    @Override
    public void presence(CurioContext context) {
        for (var index = 0; index < modifiers.size(); index++) {
            var modifier = modifiers.get(index);
            CurioAttributes.push(context.player(), context.stack(), index, modifier.attribute(), modifier.amount(), modifier.operation());
        }
    }

    public record Modifier(EntityAttribute attribute, double amount, EntityAttributeModifier.Operation operation) {
    }
}
