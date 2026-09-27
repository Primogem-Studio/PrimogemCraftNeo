package net.per.primogemcraft.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.WishArrowEntity;

public final class WishArrowRenderer extends ArrowRenderer<WishArrowEntity> {
    public WishArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(WishArrowEntity arrow) {
        return arrow.texture();
    }
}
