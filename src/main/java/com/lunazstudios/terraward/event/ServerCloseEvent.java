package com.lunazstudios.terraward.event;

import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class ServerCloseEvent implements ServerWorldEvents.Unload {

    /**
     * EventCallback for World unloading.
     */
    @Override
    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        ModConfig config = ModConfigManager.getConfig(server);
        config.save();
    }
}
