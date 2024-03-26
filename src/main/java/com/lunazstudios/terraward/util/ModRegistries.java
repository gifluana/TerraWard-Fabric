package com.lunazstudios.terraward.util;

import com.lunazstudios.terraward.command.AreaCommand;
import com.lunazstudios.terraward.config.ModConfigManager;
import com.lunazstudios.terraward.event.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ModRegistries {

    /**
     * Helper method to register callbacks.
     */
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(AreaCommand::register);
    }

    private static void registerEvents() {
        ModConfigManager.registerEvents();
        UseBlockCallback.EVENT.register(new NoPlaceEvent());
        PlayerBlockBreakEvents.BEFORE.register(new NoBreakEvent());
        AttackEntityCallback.EVENT.register(new AttackEntityEvent());
        ServerPlayerEvents.AFTER_RESPAWN.register(new AfterRespawnEvent());
        ServerPlayConnectionEvents.JOIN.register(new AfterLoginEvent());
        ServerWorldEvents.LOAD.register(new ServerLoadEvent());
        ServerWorldEvents.UNLOAD.register(new ServerCloseEvent());
    }
}
