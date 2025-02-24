package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_TABLE = (p_326304_, p_326305_) -> {
        ReloadableServerRegistries.Holder reloadableserverregistries$holder = p_326304_.getSource().getServer().reloadableRegistries();
        return SharedSuggestionProvider.suggestResource(reloadableserverregistries$holder.getKeys(Registries.LOOT_TABLE), p_326305_);
    };
    private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType(
        p_308774_ -> Component.translatableEscape("commands.drop.no_held_items", p_308774_)
    );
    private static final DynamicCommandExceptionType ERROR_NO_ENTITY_LOOT_TABLE = new DynamicCommandExceptionType(
        p_308775_ -> Component.translatableEscape("commands.drop.no_loot_table.entity", p_308775_)
    );
    private static final DynamicCommandExceptionType ERROR_NO_BLOCK_LOOT_TABLE = new DynamicCommandExceptionType(
        p_358608_ -> Component.translatableEscape("commands.drop.no_loot_table.block", p_358608_)
    );

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(
            addTargets(
                Commands.literal("loot").requires(p_137937_ -> p_137937_.hasPermission(2)),
                (p_214520_, p_214521_) -> p_214520_.then(
                            Commands.literal("fish")
                                .then(
                                    Commands.argument("loot_table", ResourceOrIdArgument.lootTable(pContext))
                                        .suggests(SUGGEST_LOOT_TABLE)
                                        .then(
                                            Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(
                                                    p_326309_ -> dropFishingLoot(
                                                            p_326309_,
                                                            ResourceOrIdArgument.getLootTable(p_326309_, "loot_table"),
                                                            BlockPosArgument.getLoadedBlockPos(p_326309_, "pos"),
                                                            ItemStack.EMPTY,
                                                            p_214521_
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("tool", ItemArgument.item(pContext))
                                                        .executes(
                                                            p_326303_ -> dropFishingLoot(
                                                                    p_326303_,
                                                                    ResourceOrIdArgument.getLootTable(p_326303_, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(p_326303_, "pos"),
                                                                    ItemArgument.getItem(p_326303_, "tool").createItemStack(1, false),
                                                                    p_214521_
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("mainhand")
                                                        .executes(
                                                            p_326295_ -> dropFishingLoot(
                                                                    p_326295_,
                                                                    ResourceOrIdArgument.getLootTable(p_326295_, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(p_326295_, "pos"),
                                                                    getSourceHandItem(p_326295_.getSource(), EquipmentSlot.MAINHAND),
                                                                    p_214521_
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("offhand")
                                                        .executes(
                                                            p_326299_ -> dropFishingLoot(
                                                                    p_326299_,
                                                                    ResourceOrIdArgument.getLootTable(p_326299_, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(p_326299_, "pos"),
                                                                    getSourceHandItem(p_326299_.getSource(), EquipmentSlot.OFFHAND),
                                                                    p_214521_
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("loot")
                                .then(
                                    Commands.argument("loot_table", ResourceOrIdArgument.lootTable(pContext))
                                        .suggests(SUGGEST_LOOT_TABLE)
                                        .executes(p_326301_ -> dropChestLoot(p_326301_, ResourceOrIdArgument.getLootTable(p_326301_, "loot_table"), p_214521_))
                                )
                        )
                        .then(
                            Commands.literal("kill")
                                .then(
                                    Commands.argument("target", EntityArgument.entity())
                                        .executes(p_180406_ -> dropKillLoot(p_180406_, EntityArgument.getEntity(p_180406_, "target"), p_214521_))
                                )
                        )
                        .then(
                            Commands.literal("mine")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(
                                            p_180403_ -> dropBlockLoot(p_180403_, BlockPosArgument.getLoadedBlockPos(p_180403_, "pos"), ItemStack.EMPTY, p_214521_)
                                        )
                                        .then(
                                            Commands.argument("tool", ItemArgument.item(pContext))
                                                .executes(
                                                    p_180400_ -> dropBlockLoot(
                                                            p_180400_,
                                                            BlockPosArgument.getLoadedBlockPos(p_180400_, "pos"),
                                                            ItemArgument.getItem(p_180400_, "tool").createItemStack(1, false),
                                                            p_214521_
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("mainhand")
                                                .executes(
                                                    p_180397_ -> dropBlockLoot(
                                                            p_180397_,
                                                            BlockPosArgument.getLoadedBlockPos(p_180397_, "pos"),
                                                            getSourceHandItem(p_180397_.getSource(), EquipmentSlot.MAINHAND),
                                                            p_214521_
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("offhand")
                                                .executes(
                                                    p_180394_ -> dropBlockLoot(
                                                            p_180394_,
                                                            BlockPosArgument.getLoadedBlockPos(p_180394_, "pos"),
                                                            getSourceHandItem(p_180394_.getSource(), EquipmentSlot.OFFHAND),
                                                            p_214521_
                                                        )
                                                )
                                        )
                                )
                        )
            )
        );
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addTargets(T pBuilder, LootCommand.TailProvider pTailProvider) {
        return pBuilder.then(
                Commands.literal("replace")
                    .then(
                        Commands.literal("entity")
                            .then(
                                Commands.argument("entities", EntityArgument.entities())
                                    .then(
                                        pTailProvider.construct(
                                                Commands.argument("slot", SlotArgument.slot()),
                                                (p_138032_, p_138033_, p_138034_) -> entityReplace(
                                                        EntityArgument.getEntities(p_138032_, "entities"),
                                                        SlotArgument.getSlot(p_138032_, "slot"),
                                                        p_138033_.size(),
                                                        p_138033_,
                                                        p_138034_
                                                    )
                                            )
                                            .then(
                                                pTailProvider.construct(
                                                    Commands.argument("count", IntegerArgumentType.integer(0)),
                                                    (p_138025_, p_138026_, p_138027_) -> entityReplace(
                                                            EntityArgument.getEntities(p_138025_, "entities"),
                                                            SlotArgument.getSlot(p_138025_, "slot"),
                                                            IntegerArgumentType.getInteger(p_138025_, "count"),
                                                            p_138026_,
                                                            p_138027_
                                                        )
                                                )
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("block")
                            .then(
                                Commands.argument("targetPos", BlockPosArgument.blockPos())
                                    .then(
                                        pTailProvider.construct(
                                                Commands.argument("slot", SlotArgument.slot()),
                                                (p_138018_, p_138019_, p_138020_) -> blockReplace(
                                                        p_138018_.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(p_138018_, "targetPos"),
                                                        SlotArgument.getSlot(p_138018_, "slot"),
                                                        p_138019_.size(),
                                                        p_138019_,
                                                        p_138020_
                                                    )
                                            )
                                            .then(
                                                pTailProvider.construct(
                                                    Commands.argument("count", IntegerArgumentType.integer(0)),
                                                    (p_138011_, p_138012_, p_138013_) -> blockReplace(
                                                            p_138011_.getSource(),
                                                            BlockPosArgument.getLoadedBlockPos(p_138011_, "targetPos"),
                                                            IntegerArgumentType.getInteger(p_138011_, "slot"),
                                                            IntegerArgumentType.getInteger(p_138011_, "count"),
                                                            p_138012_,
                                                            p_138013_
                                                        )
                                                )
                                            )
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("insert")
                    .then(
                        pTailProvider.construct(
                            Commands.argument("targetPos", BlockPosArgument.blockPos()),
                            (p_138004_, p_138005_, p_138006_) -> blockDistribute(
                                    p_138004_.getSource(), BlockPosArgument.getLoadedBlockPos(p_138004_, "targetPos"), p_138005_, p_138006_
                                )
                        )
                    )
            )
            .then(
                Commands.literal("give")
                    .then(
                        pTailProvider.construct(
                            Commands.argument("players", EntityArgument.players()),
                            (p_137992_, p_137993_, p_137994_) -> playerGive(EntityArgument.getPlayers(p_137992_, "players"), p_137993_, p_137994_)
                        )
                    )
            )
            .then(
                Commands.literal("spawn")
                    .then(
                        pTailProvider.construct(
                            Commands.argument("targetPos", Vec3Argument.vec3()),
                            (p_137918_, p_137919_, p_137920_) -> dropInWorld(
                                    p_137918_.getSource(), Vec3Argument.getVec3(p_137918_, "targetPos"), p_137919_, p_137920_
                                )
                        )
                    )
            );
    }

    private static Container getContainer(CommandSourceStack pSource, BlockPos pPos) throws CommandSyntaxException {
        BlockEntity blockentity = pSource.getLevel().getBlockEntity(pPos);
        if (!(blockentity instanceof Container)) {
            throw ItemCommands.ERROR_TARGET_NOT_A_CONTAINER.create(pPos.getX(), pPos.getY(), pPos.getZ());
        } else {
            return (Container)blockentity;
        }
    }

    private static int blockDistribute(CommandSourceStack pSource, BlockPos pPos, List<ItemStack> pItems, LootCommand.Callback pCallback) throws CommandSyntaxException {
        Container container = getContainer(pSource, pPos);
        List<ItemStack> list = Lists.newArrayListWithCapacity(pItems.size());

        for (ItemStack itemstack : pItems) {
            if (distributeToContainer(container, itemstack.copy())) {
                container.setChanged();
                list.add(itemstack);
            }
        }

        pCallback.accept(list);
        return list.size();
    }

    private static boolean distributeToContainer(Container pContainer, ItemStack pItem) {
        boolean flag = false;

        for (int i = 0; i < pContainer.getContainerSize() && !pItem.isEmpty(); i++) {
            ItemStack itemstack = pContainer.getItem(i);
            if (pContainer.canPlaceItem(i, pItem)) {
                if (itemstack.isEmpty()) {
                    pContainer.setItem(i, pItem);
                    flag = true;
                    break;
                }

                if (canMergeItems(itemstack, pItem)) {
                    int j = pItem.getMaxStackSize() - itemstack.getCount();
                    int k = Math.min(pItem.getCount(), j);
                    pItem.shrink(k);
                    itemstack.grow(k);
                    flag = true;
                }
            }
        }

        return flag;
    }

    private static int blockReplace(
        CommandSourceStack pSource, BlockPos pPos, int pSlot, int pNumSlots, List<ItemStack> pItems, LootCommand.Callback pCallback
    ) throws CommandSyntaxException {
        Container container = getContainer(pSource, pPos);
        int i = container.getContainerSize();
        if (pSlot >= 0 && pSlot < i) {
            List<ItemStack> list = Lists.newArrayListWithCapacity(pItems.size());

            for (int j = 0; j < pNumSlots; j++) {
                int k = pSlot + j;
                ItemStack itemstack = j < pItems.size() ? pItems.get(j) : ItemStack.EMPTY;
                if (container.canPlaceItem(k, itemstack)) {
                    container.setItem(k, itemstack);
                    list.add(itemstack);
                }
            }

            pCallback.accept(list);
            return list.size();
        } else {
            throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create(pSlot);
        }
    }

    private static boolean canMergeItems(ItemStack pFirst, ItemStack pSecond) {
        return pFirst.getCount() <= pFirst.getMaxStackSize() && ItemStack.isSameItemSameComponents(pFirst, pSecond);
    }

    private static int playerGive(Collection<ServerPlayer> pTargets, List<ItemStack> pItems, LootCommand.Callback pCallback) throws CommandSyntaxException {
        List<ItemStack> list = Lists.newArrayListWithCapacity(pItems.size());

        for (ItemStack itemstack : pItems) {
            for (ServerPlayer serverplayer : pTargets) {
                if (serverplayer.getInventory().add(itemstack.copy())) {
                    list.add(itemstack);
                }
            }
        }

        pCallback.accept(list);
        return list.size();
    }

    private static void setSlots(Entity pTarget, List<ItemStack> pItems, int pStartSlot, int pNumSlots, List<ItemStack> pSetItems) {
        for (int i = 0; i < pNumSlots; i++) {
            ItemStack itemstack = i < pItems.size() ? pItems.get(i) : ItemStack.EMPTY;
            SlotAccess slotaccess = pTarget.getSlot(pStartSlot + i);
            if (slotaccess != SlotAccess.NULL && slotaccess.set(itemstack.copy())) {
                pSetItems.add(itemstack);
            }
        }
    }

    private static int entityReplace(
        Collection<? extends Entity> pTargets, int pStartSlot, int pNumSlots, List<ItemStack> pItems, LootCommand.Callback pCallback
    ) throws CommandSyntaxException {
        List<ItemStack> list = Lists.newArrayListWithCapacity(pItems.size());

        for (Entity entity : pTargets) {
            if (entity instanceof ServerPlayer serverplayer) {
                setSlots(entity, pItems, pStartSlot, pNumSlots, list);
                serverplayer.containerMenu.broadcastChanges();
            } else {
                setSlots(entity, pItems, pStartSlot, pNumSlots, list);
            }
        }

        pCallback.accept(list);
        return list.size();
    }

    private static int dropInWorld(CommandSourceStack pSource, Vec3 pPos, List<ItemStack> pItems, LootCommand.Callback pCallback) throws CommandSyntaxException {
        ServerLevel serverlevel = pSource.getLevel();
        pItems.forEach(p_137884_ -> {
            ItemEntity itementity = new ItemEntity(serverlevel, pPos.x, pPos.y, pPos.z, p_137884_.copy());
            itementity.setDefaultPickUpDelay();
            serverlevel.addFreshEntity(itementity);
        });
        pCallback.accept(pItems);
        return pItems.size();
    }

    private static void callback(CommandSourceStack pSource, List<ItemStack> pItems) {
        if (pItems.size() == 1) {
            ItemStack itemstack = pItems.get(0);
            pSource.sendSuccess(() -> Component.translatable("commands.drop.success.single", itemstack.getCount(), itemstack.getDisplayName()), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.drop.success.multiple", pItems.size()), false);
        }
    }

    private static void callback(CommandSourceStack pSource, List<ItemStack> pItems, ResourceKey<LootTable> pLootTable) {
        if (pItems.size() == 1) {
            ItemStack itemstack = pItems.get(0);
            pSource.sendSuccess(
                () -> Component.translatable(
                        "commands.drop.success.single_with_table", itemstack.getCount(), itemstack.getDisplayName(), Component.translationArg(pLootTable.location())
                    ),
                false
            );
        } else {
            pSource.sendSuccess(
                () -> Component.translatable("commands.drop.success.multiple_with_table", pItems.size(), Component.translationArg(pLootTable.location())), false
            );
        }
    }

    private static ItemStack getSourceHandItem(CommandSourceStack pSource, EquipmentSlot pSlot) throws CommandSyntaxException {
        Entity entity = pSource.getEntityOrException();
        if (entity instanceof LivingEntity) {
            return ((LivingEntity)entity).getItemBySlot(pSlot);
        } else {
            throw ERROR_NO_HELD_ITEMS.create(entity.getDisplayName());
        }
    }

    private static int dropBlockLoot(CommandContext<CommandSourceStack> pContext, BlockPos pPos, ItemStack pTool, LootCommand.DropConsumer pDropConsumer) throws CommandSyntaxException {
        CommandSourceStack commandsourcestack = pContext.getSource();
        ServerLevel serverlevel = commandsourcestack.getLevel();
        BlockState blockstate = serverlevel.getBlockState(pPos);
        BlockEntity blockentity = serverlevel.getBlockEntity(pPos);
        Optional<ResourceKey<LootTable>> optional = blockstate.getBlock().getLootTable();
        if (optional.isEmpty()) {
            throw ERROR_NO_BLOCK_LOOT_TABLE.create(blockstate.getBlock().getName());
        } else {
            LootParams.Builder lootparams$builder = new LootParams.Builder(serverlevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
                .withParameter(LootContextParams.BLOCK_STATE, blockstate)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity())
                .withParameter(LootContextParams.TOOL, pTool);
            List<ItemStack> list = blockstate.getDrops(lootparams$builder);
            return pDropConsumer.accept(pContext, list, p_358604_ -> callback(commandsourcestack, p_358604_, optional.get()));
        }
    }

    private static int dropKillLoot(CommandContext<CommandSourceStack> pContext, Entity pEntity, LootCommand.DropConsumer pDropConsumer) throws CommandSyntaxException {
        Optional<ResourceKey<LootTable>> optional = pEntity.getLootTable();
        if (optional.isEmpty()) {
            throw ERROR_NO_ENTITY_LOOT_TABLE.create(pEntity.getDisplayName());
        } else {
            CommandSourceStack commandsourcestack = pContext.getSource();
            LootParams.Builder lootparams$builder = new LootParams.Builder(commandsourcestack.getLevel());
            Entity entity = commandsourcestack.getEntity();
            if (entity instanceof Player player) {
                lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
            }

            lootparams$builder.withParameter(LootContextParams.DAMAGE_SOURCE, pEntity.damageSources().magic());
            lootparams$builder.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, entity);
            lootparams$builder.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, entity);
            lootparams$builder.withParameter(LootContextParams.THIS_ENTITY, pEntity);
            lootparams$builder.withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition());
            LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
            LootTable loottable = commandsourcestack.getServer().reloadableRegistries().getLootTable(optional.get());
            List<ItemStack> list = loottable.getRandomItems(lootparams);
            return pDropConsumer.accept(pContext, list, p_358607_ -> callback(commandsourcestack, p_358607_, optional.get()));
        }
    }

    private static int dropChestLoot(CommandContext<CommandSourceStack> pContext, Holder<LootTable> pLootTable, LootCommand.DropConsumer pDropCOnsimer) throws CommandSyntaxException {
        CommandSourceStack commandsourcestack = pContext.getSource();
        LootParams lootparams = new LootParams.Builder(commandsourcestack.getLevel())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity())
            .withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition())
            .create(LootContextParamSets.CHEST);
        return drop(pContext, pLootTable, lootparams, pDropCOnsimer);
    }

    private static int dropFishingLoot(
        CommandContext<CommandSourceStack> pContext, Holder<LootTable> pLootTable, BlockPos pPos, ItemStack pTool, LootCommand.DropConsumer pDropConsumet
    ) throws CommandSyntaxException {
        CommandSourceStack commandsourcestack = pContext.getSource();
        LootParams lootparams = new LootParams.Builder(commandsourcestack.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
            .withParameter(LootContextParams.TOOL, pTool)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity())
            .create(LootContextParamSets.FISHING);
        return drop(pContext, pLootTable, lootparams, pDropConsumet);
    }

    private static int drop(
        CommandContext<CommandSourceStack> pContext, Holder<LootTable> pLootTable, LootParams pParams, LootCommand.DropConsumer pDropConsumer
    ) throws CommandSyntaxException {
        CommandSourceStack commandsourcestack = pContext.getSource();
        List<ItemStack> list = pLootTable.value().getRandomItems(pParams);
        return pDropConsumer.accept(pContext, list, p_137997_ -> callback(commandsourcestack, p_137997_));
    }

    @FunctionalInterface
    interface Callback {
        void accept(List<ItemStack> pItems) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface DropConsumer {
        int accept(CommandContext<CommandSourceStack> pContext, List<ItemStack> pItems, LootCommand.Callback pCallback) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface TailProvider {
        ArgumentBuilder<CommandSourceStack, ?> construct(ArgumentBuilder<CommandSourceStack, ?> pBuilder, LootCommand.DropConsumer pDropConsumer);
    }
}