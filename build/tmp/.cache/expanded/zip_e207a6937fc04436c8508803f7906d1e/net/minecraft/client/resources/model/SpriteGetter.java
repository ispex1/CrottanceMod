package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpriteGetter {
    TextureAtlasSprite get(Material p_376066_);

    TextureAtlasSprite reportMissingReference(String p_377389_);

    default TextureAtlasSprite maybeMissing(net.minecraft.client.renderer.block.model.TextureSlots textures, String name) {
        var material = textures.getMaterial(name);
        return material != null ? get(material) : reportMissingReference(name);
    }
}
