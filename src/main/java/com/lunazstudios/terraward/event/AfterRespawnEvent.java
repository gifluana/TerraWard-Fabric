package com.lunazstudios.terraward.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class AfterRespawnEvent implements ServerPlayerEvents.AfterRespawn {

    /**
     * EventCallback for when a Player respawns.
     */
    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {

    }
}
