package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerActionPacket> STREAM_CODEC = Packet.codec(
        ServerboundPlayerActionPacket::write, ServerboundPlayerActionPacket::new
    );
    private final BlockPos pos;
    private final Direction direction;
    private final ServerboundPlayerActionPacket.Action action;
    private final int sequence;

    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action pAction, BlockPos pPos, Direction pDirection, int pSequence) {
        this.action = pAction;
        this.pos = pPos.immutable();
        this.direction = pDirection;
        this.sequence = pSequence;
    }

    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action pAction, BlockPos pPos, Direction pDirection) {
        this(pAction, pPos, pDirection, 0);
    }

    private ServerboundPlayerActionPacket(FriendlyByteBuf pBuffer) {
        this.action = pBuffer.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.pos = pBuffer.readBlockPos();
        this.direction = Direction.from3DDataValue(pBuffer.readUnsignedByte());
        this.sequence = pBuffer.readVarInt();
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeEnum(this.action);
        pBuffer.writeBlockPos(this.pos);
        pBuffer.writeByte(this.direction.get3DDataValue());
        pBuffer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<ServerboundPlayerActionPacket> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_ACTION;
    }

    public void handle(ServerGamePacketListener pHandler) {
        pHandler.handlePlayerAction(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ServerboundPlayerActionPacket.Action getAction() {
        return this.action;
    }

    public int getSequence() {
        return this.sequence;
    }

    public static enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND;
    }
}