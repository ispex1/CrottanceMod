package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class StructureTemplatePool {
    private static final int SIZE_UNSET = Integer.MIN_VALUE;
    private static final MutableObject<Codec<Holder<StructureTemplatePool>>> CODEC_REFERENCE = new MutableObject<>();
    public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(
        p_327478_ -> p_327478_.group(
                    Codec.lazyInitialized(CODEC_REFERENCE::getValue).fieldOf("fallback").forGetter(StructureTemplatePool::getFallback),
                    Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight"))
                        .codec()
                        .listOf()
                        .fieldOf("elements")
                        .forGetter(p_210579_ -> p_210579_.rawTemplates)
                )
                .apply(p_327478_, StructureTemplatePool::new)
    );
    public static final Codec<Holder<StructureTemplatePool>> CODEC = Util.make(
        RegistryFileCodec.create(Registries.TEMPLATE_POOL, DIRECT_CODEC), CODEC_REFERENCE::setValue
    );
    private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final ObjectArrayList<StructurePoolElement> templates;
    private final Holder<StructureTemplatePool> fallback;
    private int maxSize = Integer.MIN_VALUE;

    public StructureTemplatePool(Holder<StructureTemplatePool> pFallback, List<Pair<StructurePoolElement, Integer>> pRawTemplates) {
        this.rawTemplates = pRawTemplates;
        this.templates = new ObjectArrayList<>();

        for (Pair<StructurePoolElement, Integer> pair : pRawTemplates) {
            StructurePoolElement structurepoolelement = pair.getFirst();

            for (int i = 0; i < pair.getSecond(); i++) {
                this.templates.add(structurepoolelement);
            }
        }

        this.fallback = pFallback;
    }

    public StructureTemplatePool(
        Holder<StructureTemplatePool> pFallback,
        List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> pRawTemplateFactories,
        StructureTemplatePool.Projection pProjection
    ) {
        this.rawTemplates = Lists.newArrayList();
        this.templates = new ObjectArrayList<>();

        for (Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> pair : pRawTemplateFactories) {
            StructurePoolElement structurepoolelement = pair.getFirst().apply(pProjection);
            this.rawTemplates.add(Pair.of(structurepoolelement, pair.getSecond()));

            for (int i = 0; i < pair.getSecond(); i++) {
                this.templates.add(structurepoolelement);
            }
        }

        this.fallback = pFallback;
    }

    public int getMaxSize(StructureTemplateManager pStructureTemplateManager) {
        if (this.maxSize == Integer.MIN_VALUE) {
            this.maxSize = this.templates
                .stream()
                .filter(p_210577_ -> p_210577_ != EmptyPoolElement.INSTANCE)
                .mapToInt(p_227361_ -> p_227361_.getBoundingBox(pStructureTemplateManager, BlockPos.ZERO, Rotation.NONE).getYSpan())
                .max()
                .orElse(0);
        }

        return this.maxSize;
    }

    public Holder<StructureTemplatePool> getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(RandomSource pRandom) {
        return (StructurePoolElement)(this.templates.isEmpty() ? EmptyPoolElement.INSTANCE : this.templates.get(pRandom.nextInt(this.templates.size())));
    }

    public List<StructurePoolElement> getShuffledTemplates(RandomSource pRandom) {
        return Util.shuffledCopy(this.templates, pRandom);
    }

    public int size() {
        return this.templates.size();
    }

    public static enum Projection implements StringRepresentable {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        public static final StringRepresentable.EnumCodec<StructureTemplatePool.Projection> CODEC = StringRepresentable.fromEnum(
            StructureTemplatePool.Projection::values
        );
        private final String name;
        private final ImmutableList<StructureProcessor> processors;

        private Projection(final String pName, final ImmutableList<StructureProcessor> pProcessors) {
            this.name = pName;
            this.processors = pProcessors;
        }

        public String getName() {
            return this.name;
        }

        public static StructureTemplatePool.Projection byName(String pName) {
            return CODEC.byName(pName);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}