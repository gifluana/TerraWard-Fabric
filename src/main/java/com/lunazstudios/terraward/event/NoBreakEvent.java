package com.lunazstudios.terraward.event;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import com.lunazstudios.terraward.util.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NoBreakEvent implements PlayerBlockBreakEvents.Before {

    /**
     * EventCallback for when a Player breaks a block in the world.
     */
    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        MinecraftServer server = player.getServer();
        ModConfig config = ModConfigManager.getConfig(server);
        ProtectedArea area = config.findAreaByLocation(pos.getX(), pos.getY(), pos.getZ());

        if (area == null || !area.getFlagState("no-break")) {
            return true;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            player.sendMessage(TextUtil.colorize("&c&l[TerraWard]&r: &7Você não tem permissão para quebrar blocos aqui!"), false);
            return false;
        }


        serverPlayer.sendMessage(TextUtil.colorize("&c&l[TerraWard]&r: &7Você não pode quebrar blocos nesta área protegida."), false);
        return false;
    }
}
