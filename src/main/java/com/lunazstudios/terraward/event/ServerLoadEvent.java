package com.lunazstudios.terraward.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class ServerLoadEvent implements ServerWorldEvents.Load {

    /**
     * EventCallback for World loading.
     */
    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {

    }
}
