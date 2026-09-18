package net.per.primogemcraft.system.curio;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

public class AttributeCurioItem extends CurioItem {
    private final List<Modifier> modifiers;

    public AttributeCurioItem(CurioForm form, List<Modifier> modifiers, Properties properties) {
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

    public record Modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
    }
}
