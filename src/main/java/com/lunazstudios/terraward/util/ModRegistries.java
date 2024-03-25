package com.lunazstudios.terraward.util;

import com.lunazstudios.escondeesconde.command.GameCommand;
import com.lunazstudios.escondeesconde.event.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(GameCommand::register);
    }

    private static void registerEvents() {
        NoBreakEvent.register();
        ServerCloseEvent.register();
        ServerLoadEvent.register();
        UseBlockCallback.EVENT.register(new NoPlaceEvent());
        ServerPlayerEvents.AFTER_RESPAWN.register(new AfterRespawnEvent());
        ServerPlayConnectionEvents.JOIN.register(new AfterLoginEvent());
        AttackEntityCallback.EVENT.register(new AttackEntityEvent());

    }
}
