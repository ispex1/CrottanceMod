package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
    public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(
        p_225468_ -> p_225468_.group(
                    BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(p_161248_ -> p_161248_.trunkProvider),
                    TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(p_161246_ -> p_161246_.trunkPlacer),
                    BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(p_161244_ -> p_161244_.foliageProvider),
                    FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(p_191357_ -> p_191357_.foliagePlacer),
                    RootPlacer.CODEC.optionalFieldOf("root_placer").forGetter(p_225478_ -> p_225478_.rootPlacer),
                    BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter(p_225476_ -> p_225476_.dirtProvider),
                    FeatureSize.CODEC.fieldOf("minimum_size").forGetter(p_225474_ -> p_225474_.minimumSize),
                    TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(p_225472_ -> p_225472_.decorators),
                    Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(p_161232_ -> p_161232_.ignoreVines),
                    Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter(p_225470_ -> p_225470_.forceDirt)
                )
                .apply(p_225468_, TreeConfiguration::new)
    );
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider dirtProvider;
    public final TrunkPlacer trunkPlacer;
    public final BlockStateProvider foliageProvider;
    public final FoliagePlacer foliagePlacer;
    public final Optional<RootPlacer> rootPlacer;
    public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators;
    public final boolean ignoreVines;
    public final boolean forceDirt;

    protected TreeConfiguration(
        BlockStateProvider pTrunkProvider,
        TrunkPlacer pTrunkPlacer,
        BlockStateProvider pFoliageProvider,
        FoliagePlacer pFoliagePlacer,
        Optional<RootPlacer> pRootPlacer,
        BlockStateProvider pDirtProvider,
        FeatureSize pMinimumSize,
        List<TreeDecorator> pDecorators,
        boolean pIgnoreVines,
        boolean pForceDirt
    ) {
        this.trunkProvider = pTrunkProvider;
        this.trunkPlacer = pTrunkPlacer;
        this.foliageProvider = pFoliageProvider;
        this.foliagePlacer = pFoliagePlacer;
        this.rootPlacer = pRootPlacer;
        this.dirtProvider = pDirtProvider;
        this.minimumSize = pMinimumSize;
        this.decorators = pDecorators;
        this.ignoreVines = pIgnoreVines;
        this.forceDirt = pForceDirt;
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        private final TrunkPlacer trunkPlacer;
        public final BlockStateProvider foliageProvider;
        private final FoliagePlacer foliagePlacer;
        private final Optional<RootPlacer> rootPlacer;
        private BlockStateProvider dirtProvider;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private boolean ignoreVines;
        private boolean forceDirt;

        public TreeConfigurationBuilder(
            BlockStateProvider pTrunkProvider,
            TrunkPlacer pTrunkPlacer,
            BlockStateProvider pFoliageProvider,
            FoliagePlacer pFoliagePlacer,
            Optional<RootPlacer> pRootPlacer,
            FeatureSize pMinimumSize
        ) {
            this.trunkProvider = pTrunkProvider;
            this.trunkPlacer = pTrunkPlacer;
            this.foliageProvider = pFoliageProvider;
            this.dirtProvider = BlockStateProvider.simple(Blocks.DIRT);
            this.foliagePlacer = pFoliagePlacer;
            this.rootPlacer = pRootPlacer;
            this.minimumSize = pMinimumSize;
        }

        public TreeConfigurationBuilder(
            BlockStateProvider pTrunkProvider, TrunkPlacer pTrunkPlacer, BlockStateProvider pFoliageProvider, FoliagePlacer pFoliagePlacer, FeatureSize pMinimumSize
        ) {
            this(pTrunkProvider, pTrunkPlacer, pFoliageProvider, pFoliagePlacer, Optional.empty(), pMinimumSize);
        }

        public TreeConfiguration.TreeConfigurationBuilder dirt(BlockStateProvider pDirtProvider) {
            this.dirtProvider = pDirtProvider;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> pDecorators) {
            this.decorators = pDecorators;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder forceDirt() {
            this.forceDirt = true;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(
                this.trunkProvider,
                this.trunkPlacer,
                this.foliageProvider,
                this.foliagePlacer,
                this.rootPlacer,
                this.dirtProvider,
                this.minimumSize,
                this.decorators,
                this.ignoreVines,
                this.forceDirt
            );
        }
    }
}