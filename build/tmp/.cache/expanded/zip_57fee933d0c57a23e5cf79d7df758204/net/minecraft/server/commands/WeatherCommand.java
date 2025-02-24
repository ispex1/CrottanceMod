package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
    private static final int DEFAULT_TIME = -1;

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(
            Commands.literal("weather")
                .requires(p_139171_ -> p_139171_.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .executes(p_264806_ -> setClear(p_264806_.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(p_264807_ -> setClear(p_264807_.getSource(), IntegerArgumentType.getInteger(p_264807_, "duration")))
                        )
                )
                .then(
                    Commands.literal("rain")
                        .executes(p_264805_ -> setRain(p_264805_.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(p_264809_ -> setRain(p_264809_.getSource(), IntegerArgumentType.getInteger(p_264809_, "duration")))
                        )
                )
                .then(
                    Commands.literal("thunder")
                        .executes(p_264808_ -> setThunder(p_264808_.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(p_264804_ -> setThunder(p_264804_.getSource(), IntegerArgumentType.getInteger(p_264804_, "duration")))
                        )
                )
        );
    }

    private static int getDuration(CommandSourceStack pSource, int pTime, IntProvider pTimeProvider) {
        return pTime == -1 ? pTimeProvider.sample(pSource.getServer().overworld().getRandom()) : pTime;
    }

    private static int setClear(CommandSourceStack pSource, int pTime) {
        pSource.getServer().overworld().setWeatherParameters(getDuration(pSource, pTime, ServerLevel.RAIN_DELAY), 0, false, false);
        pSource.sendSuccess(() -> Component.translatable("commands.weather.set.clear"), true);
        return pTime;
    }

    private static int setRain(CommandSourceStack pSource, int pTime) {
        pSource.getServer().overworld().setWeatherParameters(0, getDuration(pSource, pTime, ServerLevel.RAIN_DURATION), true, false);
        pSource.sendSuccess(() -> Component.translatable("commands.weather.set.rain"), true);
        return pTime;
    }

    private static int setThunder(CommandSourceStack pSource, int pTime) {
        pSource.getServer().overworld().setWeatherParameters(0, getDuration(pSource, pTime, ServerLevel.THUNDER_DURATION), true, true);
        pSource.sendSuccess(() -> Component.translatable("commands.weather.set.thunder"), true);
        return pTime;
    }
}