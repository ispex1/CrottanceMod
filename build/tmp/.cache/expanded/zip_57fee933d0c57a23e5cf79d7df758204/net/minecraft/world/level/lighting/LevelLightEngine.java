package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class LevelLightEngine implements LightEventListener {
    public static final int LIGHT_SECTION_PADDING = 1;
    public static final LevelLightEngine EMPTY = new LevelLightEngine();
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final LightEngine<?, ?> blockEngine;
    @Nullable
    private final LightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter pLightChunkGetter, boolean pBlockLight, boolean pSkyLight) {
        this.levelHeightAccessor = pLightChunkGetter.getLevel();
        this.blockEngine = pBlockLight ? new BlockLightEngine(pLightChunkGetter) : null;
        this.skyEngine = pSkyLight ? new SkyLightEngine(pLightChunkGetter) : null;
    }

    private LevelLightEngine() {
        this.levelHeightAccessor = LevelHeightAccessor.create(0, 0);
        this.blockEngine = null;
        this.skyEngine = null;
    }

    @Override
    public void checkBlock(BlockPos p_75823_) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(p_75823_);
        }

        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(p_75823_);
        }
    }

    @Override
    public boolean hasLightWork() {
        return this.skyEngine != null && this.skyEngine.hasLightWork() ? true : this.blockEngine != null && this.blockEngine.hasLightWork();
    }

    @Override
    public int runLightUpdates() {
        int i = 0;
        if (this.blockEngine != null) {
            i += this.blockEngine.runLightUpdates();
        }

        if (this.skyEngine != null) {
            i += this.skyEngine.runLightUpdates();
        }

        return i;
    }

    @Override
    public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(pPos, pIsEmpty);
        }

        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(pPos, pIsEmpty);
        }
    }

    @Override
    public void setLightEnabled(ChunkPos p_285439_, boolean p_285012_) {
        if (this.blockEngine != null) {
            this.blockEngine.setLightEnabled(p_285439_, p_285012_);
        }

        if (this.skyEngine != null) {
            this.skyEngine.setLightEnabled(p_285439_, p_285012_);
        }
    }

    @Override
    public void propagateLightSources(ChunkPos p_284998_) {
        if (this.blockEngine != null) {
            this.blockEngine.propagateLightSources(p_284998_);
        }

        if (this.skyEngine != null) {
            this.skyEngine.propagateLightSources(p_284998_);
        }
    }

    public LayerLightEventListener getLayerListener(LightLayer pType) {
        if (pType == LightLayer.BLOCK) {
            return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
        } else {
            return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
        }
    }

    public String getDebugData(LightLayer pLightLayer, SectionPos pSectionPos) {
        if (pLightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(pSectionPos.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(pSectionPos.asLong());
        }

        return "n/a";
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer pLightLayer, SectionPos pSectionPos) {
        if (pLightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugSectionType(pSectionPos.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugSectionType(pSectionPos.asLong());
        }

        return LayerLightSectionStorage.SectionType.EMPTY;
    }

    public void queueSectionData(LightLayer pLightLayer, SectionPos pSectionPos, @Nullable DataLayer pDataLayer) {
        if (pLightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(pSectionPos.asLong(), pDataLayer);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(pSectionPos.asLong(), pDataLayer);
        }
    }

    public void retainData(ChunkPos pPos, boolean pRetain) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(pPos, pRetain);
        }

        if (this.skyEngine != null) {
            this.skyEngine.retainData(pPos, pRetain);
        }
    }

    public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
        int i = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(pBlockPos) - pAmount;
        int j = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(pBlockPos);
        return Math.max(j, i);
    }

    public boolean lightOnInColumn(long pColumnPos) {
        return this.blockEngine == null || this.blockEngine.storage.lightOnInColumn(pColumnPos) && (this.skyEngine == null || this.skyEngine.storage.lightOnInColumn(pColumnPos));
    }

    public int getLightSectionCount() {
        return this.levelHeightAccessor.getSectionsCount() + 2;
    }

    public int getMinLightSection() {
        return this.levelHeightAccessor.getMinSectionY() - 1;
    }

    public int getMaxLightSection() {
        return this.getMinLightSection() + this.getLightSectionCount();
    }
}