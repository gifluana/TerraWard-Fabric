package com.lunazstudios.terraward.util;

import com.lunazstudios.escondeesconde.EscondeEsconde;
import com.lunazstudios.escondeesconde.config.ModConfig;
import com.lunazstudios.escondeesconde.item.ModItems;
import com.lunazstudios.escondeesconde.permission.PermissionsManager;
import com.lunazstudios.escondeesconde.sound.ModSounds;
import com.lunazstudios.escondeesconde.team.TeamManager;
import com.lunazstudios.escondeesconde.timer.TimerRunning;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameUtil {
    private static final Random random = new Random();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final Map<String, ServerPlayerEntity> seekers = new HashMap<>();
    private static final Map<String, ServerPlayerEntity> hiders = new HashMap<>();
    private static boolean gameIsRunning = false;

    private static String gameArena = null;
    private static String gameLobby = null;

    public static void startGame(MinecraftServer server) {
        List<ServerPlayerEntity> players = getAllOnlinePlayers(server);
        List<String> playerNames = new ArrayList<>();

        if (players.size() < 5) {
            while (playerNames.size() < 5) {
                for (ServerPlayerEntity player : players) {
                    playerNames.add(player.getName().getString());
                    if (playerNames.size() == 5) break;
                }
            }
            Collections.shuffle(playerNames);
        } else {
            Collections.shuffle(players);
            playerNames = players.subList(0, 5).stream()
                    .map(player -> player.getName().getString())
                    .collect(Collectors.toList());
        }

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        for (String playerName : playerNames) {
            future = future.thenCompose(aVoid ->
                    CompletableFuture.runAsync(() ->
                                            showTitleToAllPlayers(server, "&a" + playerName, "&fEscolhendo o &cProcurador&f...", ModSounds.UI_SELECTOR, 1.0f,1.0f),
                                    server)
                            .thenCompose(aVoid2 ->
                                    CompletableFuture.supplyAsync(() -> {
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(500); // 500 ms for 10 ticks
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                        return null;
                                    }, scheduler))
            );
        }

        future.thenRunAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String seekerName = chooseSeeker(server);
            showTitleToAllPlayers(server, "&c" + seekerName,"&fé o(a) &cProcurador&f(&ca&f)&f...", ModSounds.UI_SELECTED, 1.0f,1.0f);

            TeamManager teamManager = EscondeEsconde.getTeamManager();
            ModConfig config = ModConfig.getInstance();
            String escondedorPointName = config.getEscondedorPointName(gameArena);
            String procuradorPointName = config.getProcuradorPointName(gameArena);

            chooseSeeker(server);

            setGameRunning(server,true);
            TimerRunning.startTimer(server);

            getSeekers().forEach((playerName, serverPlayer) -> {
                teamManager.addPlayerToTeam(playerName, EscondeEsconde.TEAM_PROCURADOR);
                tpPlayerToPoint(playerName, procuradorPointName);
                serverPlayer.getInventory().clear();
                giveItemToPlayer(serverPlayer, ModItems.SEEKER_STICK.getDefaultStack(), 0);
                setPlayerArmor(serverPlayer, ModItems.SEEKER_CHESTPLATE.getDefaultStack(), 1);
                serverPlayer.setHealth(20.0f);
            });

            getHiders().forEach((playerName, serverPlayer) -> {
                teamManager.addPlayerToTeam(playerName, EscondeEsconde.TEAM_ESCONDEDOR);
                tpPlayerToPoint(playerName, escondedorPointName);
                serverPlayer.getInventory().clear();

                ItemStack enchantedStick = ModItems.HIDER_STICK.getDefaultStack();
                Map<Enchantment, Integer> enchantments = Map.of(Enchantments.KNOCKBACK, 3);  // Example enchantment
                EnchantmentHelper.set(enchantments, enchantedStick);

                giveItemToPlayer(serverPlayer, enchantedStick, 0);
                setPlayerArmor(serverPlayer, ModItems.HIDER_CHESTPLATE.getDefaultStack(), 1);
                serverPlayer.setHealth(20.0f);
            });
        }, server);
    }

    private static void showTitleToAllPlayers(MinecraftServer server, String titleText, String subtitleText, SoundEvent soundEvent, float volume, float pitch) {
        Text subtitle = Text.of(TextUtil.colorize(subtitleText));
        Text title = Text.of(TextUtil.colorize("&f&l> &r" + titleText + " &f&l<"));

        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        SubtitleS2CPacket subtitlePacket = new SubtitleS2CPacket(subtitle);

        for (ServerPlayerEntity player : getAllOnlinePlayers(server)) {
            player.networkHandler.sendPacket(titlePacket);
            player.networkHandler.sendPacket(subtitlePacket);

            // Play sound only to the specific player
            player.playSound(soundEvent, SoundCategory.PLAYERS, volume, pitch);
        }
    }

    /**
     * Gives an item to a player or sets it in a specific slot.
     *
     * @param player The player to receive the item.
     * @param itemStack The item stack to give.
     * @param slotIndex The index of the slot where the item should be placed. If -1, the item is simply added to the inventory.
     */
    public static void giveItemToPlayer(ServerPlayerEntity player, ItemStack itemStack, int slotIndex) {
        if (slotIndex >= 0 && slotIndex < player.getInventory().size()) {
            player.getInventory().setStack(slotIndex, itemStack);
        } else {
            boolean added = player.getInventory().insertStack(itemStack);

            if (!added) {
                player.dropItem(itemStack, false);
            }
        }

        player.playerScreenHandler.sendContentUpdates();
    }

    /**
     * Specifically sets an armor item for the player.
     *
     * @param player The player to equip.
     * @param itemStack The armor item to equip.
     * @param armorType The type of armor (0 for boots, 1 for leggings, 2 for chestplate, 3 for helmet).
     */
    public static void setPlayerArmor(ServerPlayerEntity player, ItemStack itemStack, int armorType) {
        if (armorType >= 0 && armorType <= 3) {
            int armorSlotIndex = player.getInventory().main.size() + player.getInventory().armor.size() - 1 - armorType;
            player.getInventory().setStack(armorSlotIndex, itemStack);

            player.playerScreenHandler.sendContentUpdates();
        }
    }

    public static List<ServerPlayerEntity> getAllOnlinePlayers(MinecraftServer server) {
        PermissionsManager permissionsManager = EscondeEsconde.getPermissionsManager();

        if (permissionsManager == null) {
            return server.getPlayerManager().getPlayerList();
        }

        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !permissionsManager.hasPermission(player, permissionsManager.perm_bypassgames))
                .collect(Collectors.toList());
    }

    public static void setGameArena(String arenaName) {
        gameArena = arenaName;
    }

    public static String getGameArena() {
        return gameArena;
    }

    public static void setGameLobby(String pointName) {
        gameLobby = pointName;
    }

    public static String getGameLobby() {
        return gameLobby;
    }

    public static void tpPlayerToPoint(String playerName, String arenaPointName) {
        MinecraftServer server = EscondeEsconde.getServer();
        ModConfig config = ModConfig.getInstance();
        Vec3d pointPosition = config.getArenaPointPosition(arenaPointName);
        RegistryKey<World> worldKey = config.getArenaPointWorldKey(arenaPointName);

        if (pointPosition == null) {
            return;
        }

        if (worldKey == null) {
            return;
        }

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);

        player.teleport(
                server.getWorld(worldKey),
                pointPosition.x, pointPosition.y, pointPosition.z,
                player.getYaw(), player.getPitch()
        );
    }

    public static void tpAllToArena(MinecraftServer server, String arenaName) {
        ModConfig config = ModConfig.getInstance();
        Vec3d position = config.getEscondedorPointLocationByArenaName(arenaName);

        if (position == null) {
            EscondeEsconde.LOGGER.warn("A posição para a arena '" + arenaName + "' não foi encontrada.");
            return;
        }

        String arenaPoint = config.getEscondedorPointName(arenaName);

        if (arenaPoint == null) {
            EscondeEsconde.LOGGER.warn("A arenapoint '" + arenaPoint + "' não foi encontrada.");
            return;
        }

        RegistryKey<World> worldKey = config.getArenaPointWorldKey(arenaPoint);

        if (worldKey == null) {
            EscondeEsconde.LOGGER.warn("O mundo para a arena '" + arenaName + "' não foi encontrado.");
            return;
        }

        try {
            for (ServerPlayerEntity player : getAllOnlinePlayers(server)) {
                player.teleport(
                        server.getWorld(worldKey),
                        position.x, position.y, position.z,
                        player.getYaw(), player.getPitch()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setGameRunning(boolean running) {
        gameIsRunning = running;
    }

    public static void setGameRunning(MinecraftServer server, boolean running) {
        gameIsRunning = running;
        // Send a packet to all clients to update the game state
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(gameIsRunning);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new Identifier(EscondeEsconde.MOD_ID, "game_state"), buf);
        }
    }

    public static boolean isGameRunning() {
        return gameIsRunning;
    }

    public static String chooseSeeker(MinecraftServer server) {
        PermissionsManager permissionsManager = EscondeEsconde.getPermissionsManager();
        List<ServerPlayerEntity> onlinePlayers;

        if (permissionsManager == null) {
            // If permissionsManager is null, consider all online players without permission checks
            onlinePlayers = server.getPlayerManager().getPlayerList();
        } else {
            // If permissionsManager is not null, filter players based on permission
            onlinePlayers = server.getPlayerManager().getPlayerList().stream()
                    .filter(player -> !permissionsManager.hasPermission(player, permissionsManager.perm_bypassgames))
                    .collect(Collectors.toList());
        }

        if (onlinePlayers.isEmpty()) {
            return null;
        }

        // Clean previous game state if necessary
        seekers.clear();
        hiders.clear();

        // Randomly choose a seeker
        ServerPlayerEntity seeker = onlinePlayers.remove(random.nextInt(onlinePlayers.size()));
        addSeeker(seeker);

        // The rest are hiders
        onlinePlayers.forEach(GameUtil::addHider);

        return seeker.getGameProfile().getName();
    }

    public static void addSeeker(ServerPlayerEntity player) {
        seekers.put(player.getGameProfile().getName(), player);
    }

    public static void addHider(ServerPlayerEntity player) {
        hiders.put(player.getGameProfile().getName(), player);
    }

    public static void removeHider(String playerName) {
        // This method removes a player from the hiders map
        if (hiders.containsKey(playerName)) {
            hiders.remove(playerName);
            // Optionally, log this action or handle any additional cleanup
        }
    }

    public static void removeSeeker(String playerName) {
        // This method removes a player from the seekers map
        if (seekers.containsKey(playerName)) {
            seekers.remove(playerName);
            // Optionally, log this action or handle any additional cleanup
        }
    }

    public static void convertHiderToSeeker(String playerName) {
        ServerPlayerEntity player = hiders.get(playerName);
        if (player != null) {
            removeHider(playerName);
            addSeeker(player);
        }
    }

    public static Map<String, ServerPlayerEntity> getSeekers() {
        return Collections.unmodifiableMap(seekers);
    }

    public static Map<String, ServerPlayerEntity> getHiders() {
        return Collections.unmodifiableMap(hiders);
    }

    public static void restartGame(MinecraftServer server) {
        TeamManager teamManager = EscondeEsconde.getTeamManager();
        setGameRunning(server, false);

        seekers.clear();
        hiders.clear();

        MutableText message = Text.literal(EscondeEsconde.MOD_NAME)
                .styled(style -> style.withColor(Formatting.RED).withBold(true))
                .append(Text.literal(": ")
                        .styled(style -> style.withColor(Formatting.WHITE).withBold(false)))
                .append(Text.literal("Reiniciando o Jogo...")
                        .styled(style -> style.withColor(Formatting.GRAY).withBold(false)));

        getAllOnlinePlayers(server).forEach(player ->
                teamManager.removePlayerFromAnyTeam(player.getGameProfile().getName())
        );

        getAllOnlinePlayers(server).forEach(player ->
                player.sendMessage(message)
        );

        chooseSeeker(server);
    }

    public static void resetGame(MinecraftServer server) {

    }

    public static void endGame(MinecraftServer server) {
        TeamManager teamManager = EscondeEsconde.getTeamManager();
        setGameRunning(server, false);

        seekers.clear();
        hiders.clear();

        MutableText message = Text.literal(EscondeEsconde.MOD_NAME)
                .styled(style -> style.withColor(Formatting.RED).withBold(true))
                .append(Text.literal(": ")
                        .styled(style -> style.withColor(Formatting.WHITE).withBold(false)))
                .append(Text.literal("O ultimo escondedor foi encontrado! FIM DE JOGO.")
                        .styled(style -> style.withColor(Formatting.GRAY).withBold(false)));

        String lobby = getGameLobby();

        getAllOnlinePlayers(server).forEach(player -> {
            teamManager.removePlayerFromAnyTeam(player.getGameProfile().getName());
            player.sendMessage(message);
            player.getInventory().clear();
            tpPlayerToPoint(player.getName().getString(), lobby);
        });


        gameArena = null;
    }

    public static void processHiderFoundBySeeker(ServerPlayerEntity killedPlayer, ServerPlayerEntity killerPlayer, MinecraftServer server) {
        // Check if the game is running
        if (!isGameRunning()) {
            return;
        }

        String killedPlayerName = killedPlayer.getGameProfile().getName();
        String killerPlayerName = killerPlayer.getGameProfile().getName();

        // Verify the killed player is indeed a hider and the killer is a seeker
        if (getHiders().containsKey(killedPlayerName) &&
                getSeekers().containsKey(killerPlayerName)) {

            // Remove the killed player from the hiders map
            removeHider(killedPlayerName);

            // Add the killed player to the seekers map
            addSeeker(killedPlayer);

            TeamManager teamManager = EscondeEsconde.getTeamManager();
            teamManager.addPlayerToTeam(killedPlayerName, EscondeEsconde.TEAM_PROCURADOR);

            killedPlayer.getInventory().clear();
            giveItemToPlayer(killedPlayer, ModItems.SEEKER_STICK.getDefaultStack(), 0);
            setPlayerArmor(killedPlayer, ModItems.SEEKER_CHESTPLATE.getDefaultStack(), 1);

            if (getHiders().isEmpty()) {
                endGame(server);
            }
        }
    }
}
