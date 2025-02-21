package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBakedModel implements BakedModel {
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
    private final List<BakedQuad> unculledFaces;
    private final Map<Direction, List<BakedQuad>> culledFaces;
    private final boolean hasAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean usesBlockLight;
    private final TextureAtlasSprite particleIcon;
    private final ItemTransforms transforms;
    /** Forge: Block render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FANCY fancy graphics} */
    protected final net.minecraftforge.client.ChunkRenderTypeSet blockRenderTypes;
    /** Forge: Block render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics} */
    protected final net.minecraftforge.client.ChunkRenderTypeSet blockRenderTypesFast;
    /** Forge: If this model's {@linkplain #blockRenderTypes block render types} are rendering cutout, to account for older leaves model JSONs */
    protected final boolean isRenderingCutout;

    /** @deprecated Forge: Use {@linkplain #SimpleBakedModel(List, Map, boolean, boolean, boolean, TextureAtlasSprite, ItemTransforms, net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup) variant with RenderTypeGroup} **/
    @Deprecated
    public SimpleBakedModel(List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_) {
        this(p_119489_, p_119490_, p_119491_, p_119492_, p_119493_, p_119494_, p_119495_, net.minecraftforge.client.RenderTypeGroup.EMPTY);
    }

    /** @deprecated Forge: Use {@linkplain #SimpleBakedModel(List, Map, boolean, boolean, boolean, TextureAtlasSprite, ItemTransforms, net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup) variant with RenderTypeGroup for fast graphics} **/
    @Deprecated(forRemoval = true, since = "1.21.4")
    public SimpleBakedModel(List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_, net.minecraftforge.client.RenderTypeGroup renderTypes) {
        this(p_119489_, p_119490_, p_119491_, p_119492_, p_119493_, p_119494_, p_119495_, renderTypes, net.minecraftforge.client.RenderTypeGroup.EMPTY);
    }

    /** Constructor with {@link net.minecraftforge.client.RenderTypeGroup RenderTypeGroup} for fancy and fast graphics. Preferred over {@link net.minecraft.client.renderer.ItemBlockRenderTypes#setRenderLayer(net.minecraft.world.level.block.Block, net.minecraft.client.renderer.RenderType) ItemBlockRenderTypes.setRenderLayer(Block, RenderType)}. */
    public SimpleBakedModel(
        List<BakedQuad> p_119489_,
        Map<Direction, List<BakedQuad>> p_119490_,
        boolean p_119491_,
        boolean p_119492_,
        boolean p_119493_,
        TextureAtlasSprite p_119494_,
        ItemTransforms p_119495_,
        net.minecraftforge.client.RenderTypeGroup renderTypes,
        net.minecraftforge.client.RenderTypeGroup renderTypesFast
    ) {
        this.unculledFaces = p_119489_;
        this.culledFaces = p_119490_;
        this.hasAmbientOcclusion = p_119491_;
        this.isGui3d = p_119493_;
        this.usesBlockLight = p_119492_;
        this.particleIcon = p_119494_;
        this.transforms = p_119495_;

        boolean hasRenderTypes = renderTypes != null && !renderTypes.isEmpty();
        boolean hasRenderTypesFast = renderTypesFast != null && !renderTypesFast.isEmpty();
        this.blockRenderTypes = hasRenderTypes ? net.minecraftforge.client.ChunkRenderTypeSet.of(renderTypes.block()) : null;
        this.blockRenderTypesFast = hasRenderTypesFast ? net.minecraftforge.client.ChunkRenderTypeSet.of(renderTypesFast.block()) : null;
        this.isRenderingCutout = hasRenderTypes && (renderTypes.block() == net.minecraft.client.renderer.RenderType.cutout() || renderTypes.block() == net.minecraft.client.renderer.RenderType.cutoutMipped());
    }

    public static BakedModel bakeElements(List<BlockElement> p_377425_, TextureSlots p_378525_, SpriteGetter p_375793_, ModelState p_376680_, boolean p_375745_, boolean p_376866_, boolean p_376846_, ItemTransforms p_376883_) {
        return bakeElements(p_377425_, p_378525_, p_375793_, p_376680_, p_375745_, p_376866_, p_376846_, p_376883_, null);
    }

    public static BakedModel bakeElements(List<BlockElement> p_377425_, TextureSlots p_378525_, SpriteGetter p_375793_, ModelState p_376680_, boolean p_375745_, boolean p_376866_, boolean p_376846_, ItemTransforms p_376883_, @Nullable net.minecraftforge.client.RenderTypeGroup renderType) {
        return bakeElements(p_377425_, p_378525_, p_375793_, p_376680_, p_375745_, p_376866_, p_376846_, p_376883_, renderType, null);
    }

    public static BakedModel bakeElements(
        List<BlockElement> p_377425_,
        TextureSlots p_378525_,
        SpriteGetter p_375793_,
        ModelState p_376680_,
        boolean p_375745_,
        boolean p_376866_,
        boolean p_376846_,
        ItemTransforms p_376883_,
        @Nullable net.minecraftforge.client.RenderTypeGroup renderType,
        @Nullable net.minecraftforge.client.RenderTypeGroup renderTypeFast
    ) {
        TextureAtlasSprite textureatlassprite = findSprite(p_375793_, p_378525_, "particle");
        SimpleBakedModel.Builder simplebakedmodel$builder = new SimpleBakedModel.Builder(p_375745_, p_376866_, p_376846_, p_376883_)
            .particle(textureatlassprite);

        for (BlockElement blockelement : p_377425_) {
            for (Direction direction : blockelement.faces.keySet()) {
                BlockElementFace blockelementface = blockelement.faces.get(direction);
                TextureAtlasSprite textureatlassprite1 = findSprite(p_375793_, p_378525_, blockelementface.texture());
                if (blockelementface.cullForDirection() == null) {
                    simplebakedmodel$builder.addUnculledFace(bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_376680_));
                } else {
                    simplebakedmodel$builder.addCulledFace(
                        Direction.rotate(p_376680_.getRotation().getMatrix(), blockelementface.cullForDirection()),
                        bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_376680_)
                    );
                }
            }
        }

        if (renderType != null && !renderType.isEmpty()) {
            if (renderTypeFast != null && !renderTypeFast.isEmpty())
                simplebakedmodel$builder.renderTypes(renderType, renderTypeFast);
            else
                simplebakedmodel$builder.renderTypes(renderType);
        }

        return simplebakedmodel$builder.build();
    }

    public static BakedQuad bakeFace(
        BlockElement p_377437_, BlockElementFace p_377187_, TextureAtlasSprite p_378098_, Direction p_377774_, ModelState p_376012_
    ) {
        return FaceBakery.bakeQuad(
            p_377437_.from, p_377437_.to, p_377187_, p_378098_, p_377774_, p_376012_, p_377437_.rotation, p_377437_.shade, p_377437_.lightEmission
        );
    }

    private static TextureAtlasSprite findSprite(SpriteGetter p_376857_, TextureSlots p_377315_, String p_377512_) {
        Material material = p_377315_.getMaterial(p_377512_);
        return material != null ? p_376857_.get(material) : p_376857_.reportMissingReference(p_377512_);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_235054_, @Nullable Direction p_235055_, RandomSource p_235056_) {
        return p_235055_ == null ? this.unculledFaces : this.culledFaces.get(p_235055_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return this.isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return this.usesBlockLight;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleIcon;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    private static final net.minecraftforge.client.ChunkRenderTypeSet SOLID_BLOCK = net.minecraftforge.client.ChunkRenderTypeSet.of(net.minecraft.client.renderer.RenderType.solid());

    @Override
    public net.minecraftforge.client.ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, net.minecraftforge.client.model.data.ModelData data) {
        if (!net.minecraft.client.renderer.ItemBlockRenderTypes.isFancy()) {
            if (blockRenderTypesFast != null)
                return blockRenderTypesFast;
            if (isRenderingCutout && state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
                return SOLID_BLOCK;
        }
        return blockRenderTypes != null ? blockRenderTypes : BakedModel.super.getRenderTypes(state, rand, data);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ImmutableList.Builder<BakedQuad> unculledFaces = ImmutableList.builder();
        private final EnumMap<Direction, ImmutableList.Builder<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final boolean hasAmbientOcclusion;
        @Nullable
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(boolean p_119521_, boolean p_119522_, boolean p_119523_, ItemTransforms p_119524_) {
            this.hasAmbientOcclusion = p_119521_;
            this.usesBlockLight = p_119522_;
            this.isGui3d = p_119523_;
            this.transforms = p_119524_;

            for (Direction direction : Direction.values()) {
                this.culledFaces.put(direction, ImmutableList.builder());
            }
        }

        public SimpleBakedModel.Builder addCulledFace(Direction p_119531_, BakedQuad p_119532_) {
            this.culledFaces.get(p_119531_).add(p_119532_);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad p_119527_) {
            this.unculledFaces.add(p_119527_);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite p_119529_) {
            this.particleIcon = p_119529_;
            return this;
        }

        public SimpleBakedModel.Builder item() {
            return this;
        }

        private net.minecraftforge.client.RenderTypeGroup renderTypes = net.minecraftforge.client.RenderTypeGroup.EMPTY;
        private net.minecraftforge.client.RenderTypeGroup renderTypesFast = net.minecraftforge.client.RenderTypeGroup.EMPTY;

        /**
         * Sets the render type to be used for this model, and will be used for any graphics setting.
         * <p>
         * If you need to set a specific render type for
         * {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics}, consider using
         * {@link #renderTypes(net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup)
         * renderTypes(RenderTypeGroup, RenderTypeGroup)} instead which allows choosing render types for both fancy and
         * fast graphics.
         *
         * @apiNote If this model is for {@linkplain net.minecraft.world.level.block.LeavesBlock leaves} and if the
         * given render type is either {@linkplain net.minecraft.client.renderer.RenderType#cutout() cutout} or
         * {@linkplain net.minecraft.client.renderer.RenderType#cutoutMipped() cutout mipped}, it will be overridden
         * with {@linkplain net.minecraft.client.renderer.RenderType#solid() solid} when fast graphics is enabled.
         * @see #renderTypes(net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup)
         * renderTypes(RenderTypeGroup, RenderTypeGroup)
         */
        public SimpleBakedModel.Builder renderTypes(net.minecraftforge.client.RenderTypeGroup renderTypes) {
            return this.renderTypes(renderTypes, net.minecraftforge.client.RenderTypeGroup.EMPTY);
        }

        /**
         * Sets the render types to be used for this model: one for
         * {@linkplain net.minecraft.client.GraphicsStatus#FANCY fancy graphics} and one for
         * {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics}.
         *
         * @see #renderTypes(net.minecraftforge.client.RenderTypeGroup)
         */
        public SimpleBakedModel.Builder renderTypes(net.minecraftforge.client.RenderTypeGroup renderTypes, net.minecraftforge.client.RenderTypeGroup renderTypesFast) {
            this.renderTypes = renderTypes;
            this.renderTypesFast = renderTypesFast;
            return this;
        }

        public BakedModel build() {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                Map<Direction, List<BakedQuad>> map = Maps.transformValues(this.culledFaces, ImmutableList.Builder::build);
                return new SimpleBakedModel(
                    // Forge: Account for render types in model JSONs
                    this.unculledFaces.build(), new EnumMap<>(map), this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.renderTypes, this.renderTypesFast
                );
            }
        }
    }
}
