package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PoiRemovedDebugPayload(BlockPos pos) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PoiRemovedDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        PoiRemovedDebugPayload::write, PoiRemovedDebugPayload::new
    );
    public static final CustomPacketPayload.Type<PoiRemovedDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_removed");

    private PoiRemovedDebugPayload(FriendlyByteBuf pBuffer) {
        this(pBuffer.readBlockPos());
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeBlockPos(this.pos);
    }

    @Override
    public CustomPacketPayload.Type<PoiRemovedDebugPayload> type() {
        return TYPE;
    }
}