package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V1481 extends NamespacedSchema {
    public V1481(int p_17712_, Schema p_17713_) {
        super(p_17712_, p_17713_);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
        pSchema.registerSimple(map, "minecraft:conduit");
        return map;
    }
}