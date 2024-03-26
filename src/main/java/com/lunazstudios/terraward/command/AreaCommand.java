package com.lunazstudios.terraward.command;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import com.lunazstudios.terraward.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AreaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(areaCommand());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> areaCommand() {
        return CommandManager.literal("terraward")
                .then(CommandManager.literal("area")
                        .then(setAreaCommand())
                        .then(setAreaPriorityCommand())
                        .then(removeAreaCommand())
                        .then(listAreasCommand()));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> setAreaPriorityCommand() {
        return CommandManager.literal("set-priority")
                .then(CommandManager.argument("areaname", StringArgumentType.string())
                        .then(CommandManager.argument("priority", IntegerArgumentType.integer())
                                .executes(context -> executeSetPriority(context.getSource(),
                                        StringArgumentType.getString(context, "areaname"),
                                        IntegerArgumentType.getInteger(context, "priority")
                                ))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> setAreaCommand() {
        return CommandManager.literal("set")
                .then(CommandManager.argument("x1", IntegerArgumentType.integer())
                        .suggests((context, builder) -> suggestPosition(context, builder, "x"))
                        .then(CommandManager.argument("y1", IntegerArgumentType.integer())
                                .suggests((context, builder) -> suggestPosition(context, builder, "y"))
                                .then(CommandManager.argument("z1", IntegerArgumentType.integer())
                                        .suggests((context, builder) -> suggestPosition(context, builder, "z"))
                                        .then(CommandManager.argument("x2", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> suggestPosition(context, builder, "x"))
                                                .then(CommandManager.argument("y2", IntegerArgumentType.integer())
                                                        .suggests((context, builder) -> suggestPosition(context, builder, "y"))
                                                        .then(CommandManager.argument("z2", IntegerArgumentType.integer())
                                                                .suggests((context, builder) -> suggestPosition(context, builder, "z"))
                                                                .then(CommandManager.argument("areaname", StringArgumentType.string())
                                                                        .then(CommandManager.argument("priority", IntegerArgumentType.integer())
                                                                                .executes(context -> executeSet(context.getSource(),
                                                                                        IntegerArgumentType.getInteger(context, "x1"),
                                                                                        IntegerArgumentType.getInteger(context, "y1"),
                                                                                        IntegerArgumentType.getInteger(context, "z1"),
                                                                                        IntegerArgumentType.getInteger(context, "x2"),
                                                                                        IntegerArgumentType.getInteger(context, "y2"),
                                                                                        IntegerArgumentType.getInteger(context, "z2"),
                                                                                        StringArgumentType.getString(context, "areaname"),
                                                                                        IntegerArgumentType.getInteger(context, "priority")
                                                                                ))

                                                                        ))))))));
    }

    /**
     * Create a literal argument builder for the remove command.
     * The remove command removes an area from the configuration.
     * The command requires an areaname argument.
     */
    private static LiteralArgumentBuilder<ServerCommandSource> removeAreaCommand() {
        return CommandManager.literal("remove")
                .then(CommandManager.argument("areaname", StringArgumentType.string())
                        .executes(context -> executeRemove(context.getSource(),
                                StringArgumentType.getString(context, "areaname"))));
    }

    /**
     * Create a literal argument builder for the list command.
     * The list command lists all areas defined in the configuration.
     * The command does not require any arguments.
     */
    private static LiteralArgumentBuilder<ServerCommandSource> listAreasCommand() {
        return CommandManager.literal("list")
                .executes(context -> executeList(context.getSource()));
    }

    /**
     * Execute the set command.
     * The set command sets a new area in the configuration.
     */
    private static int executeSet(ServerCommandSource source, int x1, int y1, int z1, int x2, int y2, int z2, String areaname, int priority) {
        MinecraftServer server = source.getServer();
        ModConfig config = ModConfigManager.getConfig(server);

        if (config != null) {
            ProtectedArea area = new ProtectedArea(areaname, x1, y1, z1, x2, y2, z2, priority);
            config.addArea(area);

            source.sendFeedback(() -> TextUtil.colorize("&a&l[TerraWard]&r: &7The area &6" + areaname + " &r&7was set successfully!"), false);
            return 1;
        } else {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7Failed to access the configuration."));
            return 0;
        }
    }

    /**
     * Suggest a position for the set command.
     * The set command requires the following arguments:
     */
    private static CompletableFuture<Suggestions> suggestPosition(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder, String axis) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        assert player != null;
        Vec3d lookingAt = getLookingAt(player);

        return switch (axis) {
            case "x" -> builder.suggest(String.valueOf((int) lookingAt.x)).buildFuture();
            case "y" -> builder.suggest(String.valueOf((int) lookingAt.y)).buildFuture();
            case "z" -> builder.suggest(String.valueOf((int) lookingAt.z)).buildFuture();
            default -> Suggestions.empty();
        };

    }

    /**
     * Get the block the player is looking at.
     * The player is the source of the command.
     * The block the player is looking at is returned as a Vec3d.
     */
    private static Vec3d getLookingAt(ServerPlayerEntity player) {
        HitResult hitResult = player.raycast(20.0, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return player.getPos();
    }

    /**
     * Execute the set-priority command.
     * The set-priority command sets the priority of an area in the configuration.
     */
    private static int executeSetPriority(ServerCommandSource source, String areaname, int priority) {
        MinecraftServer server = source.getServer();
        ModConfig config = ModConfigManager.getConfig(server);

        if (config == null) {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7Failed to access the configuration."));
            return 0;
        }

        ProtectedArea area = config.getArea(areaname);
        if (area == null) {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7The area &c" + areaname + " &r&7was not found!"));
            return 0;
        }

        area.setPriority(priority);
        config.save();

        source.sendFeedback(() -> TextUtil.colorize("&a&l[TerraWard]&r: &6" + areaname + " &r&7area priority was set to &6" + priority + "&7!"), false);
        return 1;
    }

    /**
     * Execute the remove command.
     * The remove command removes an area from the configuration.
     */
    private static int executeRemove(ServerCommandSource source, String areaname) {
        MinecraftServer server = source.getServer();
        ModConfig config = ModConfigManager.getConfig(server);

        if (config == null) {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7Failed to access the configuration."));
            return 0;
        }

        boolean removed = config.removeArea(areaname);
        if (removed) {
            config.save();
            source.sendFeedback(() -> TextUtil.colorize("&a&l[TerraWard]&r: &7The area &6" + areaname + " &r&7was removed successfully!"), false);
            return 1;
        } else {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7The area &c" + areaname + " &r&7was not found!"));
            return 0;
        }
    }

    /**
     * Execute the list command.
     * The list command lists all areas defined in the configuration.
     */
    private static int executeList(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        ModConfig config = ModConfigManager.getConfig(server);

        if (config == null) {
            source.sendError(TextUtil.colorize("&c&l[TerraWard]&r: &7Failed to access the configuration."));
            return 0;
        }

        Collection<ProtectedArea> areas = config.getAllAreas();

        if (areas.isEmpty()) {
            source.sendFeedback(() -> TextUtil.colorize("&c&l[TerraWard]&r: &7There's no areas defined!"), false);
            return 0;
        } else {
            source.sendFeedback(() -> TextUtil.colorize("&a&l[TerraWard]&r: &7Defined areas:"), false);

            areas.forEach(area -> {
                MutableText areaName = Text.literal(area.getName() + " - ")
                        .styled(style -> style.withColor(Formatting.WHITE).withBold(false));

                MutableText firstCoordinate = Text.literal("[" + area.getX1() + ", " + area.getY1() + ", " + area.getZ1() + "]")
                        .styled(style -> style.withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + area.getX1() + " " + area.getY1() + " " + area.getZ1()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Teleport to this coordinate"))));


                MutableText toText = Text.literal(" to ")
                        .styled(style -> style.withColor(Formatting.WHITE).withBold(false));

                MutableText secondCoordinate = Text.literal("[" + area.getX2() + ", " + area.getY2() + ", " + area.getZ2() + "]")
                        .styled(style -> style.withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + area.getX2() + " " + area.getY2() + " " + area.getZ2()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Teleport to this coordinate"))));

                source.sendFeedback(() -> areaName.append(firstCoordinate).append(toText).append(secondCoordinate), false);
            });

            return 1;
        }
    }
}
