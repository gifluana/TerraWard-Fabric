package com.lunazstudios.terraward.event;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import com.lunazstudios.terraward.util.TextUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AttackEntityEvent implements AttackEntityCallback {

    /**
     * EventCallback for when a Player interact with entities.
     * Specially for when the player tries to hit any entity or player.
     */
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        BlockPos pos = entity.getBlockPos();
        MinecraftServer server = player.getServer();
        ModConfig config = ModConfigManager.getConfig(server);
        ProtectedArea area = config.findAreaByLocation(pos.getX(), pos.getY(), pos.getZ());


        if (area != null && area.getFlagState("no-breakentities")) {
            if (player instanceof ServerPlayerEntity) {

                if (entity instanceof ServerPlayerEntity) {
                    return ActionResult.PASS;
                }

                player.sendMessage(TextUtil.colorize("&c&l[TerraWard]&r: &7Você não pode bater em entidades aqui!"), false);
                return ActionResult.FAIL;
            }
        }

        if (area != null && area.getFlagState("no-pvp")) {
            if (player instanceof ServerPlayerEntity) {

                if (entity instanceof ServerPlayerEntity) {
                    player.sendMessage(TextUtil.colorize("&c&l[TerraWard]&r: &7Sem pvp aqui!"), false);

                    return ActionResult.FAIL;
                }

                return ActionResult.PASS;
            }
        }

        return ActionResult.PASS;
    }
}
