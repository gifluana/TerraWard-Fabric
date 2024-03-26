package com.lunazstudios.terraward.event;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import com.lunazstudios.terraward.util.TextUtil;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoPlaceEvent implements UseBlockCallback {

    /**
     * EventCallback for when a Player interact with the world.
     * Specially for when they try to place a block or interact with blocks that isn't breaking.
     */
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();

        MinecraftServer server = player.getServer();
        ModConfig config = ModConfigManager.getConfig(server);
        ProtectedArea area = config.findAreaByLocation(pos.getX(), pos.getY(), pos.getZ());

        if (area != null) {
            if (block == Blocks.CHEST || block == Blocks.BARREL || block == Blocks.ENDER_CHEST) {
                if (area.getFlagState("no-openchest")) {
                    sendMessage(player, "&c&l[TerraWard]&r: &7Você não pode abrir baús aqui!");
                    return ActionResult.FAIL;
                }
            } else if (area.getFlagState("no-rightclick")) {
                sendMessage(player, "&c&l[TerraWard]&r: &7Você não pode interagir com blocos aqui!");
                return ActionResult.FAIL;
            }

            Item item = player.getMainHandStack().getItem();
            if (item instanceof BlockItem && area.getFlagState("no-place")) {
                sendMessage(player, "&c&l[TerraWard]&r: &7Você não pode colocar blocos aqui!");
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    private void sendMessage(PlayerEntity player, String message) {
        if (player instanceof ServerPlayerEntity) {
            player.sendMessage(TextUtil.colorize(message), false);
        }
    }
}
