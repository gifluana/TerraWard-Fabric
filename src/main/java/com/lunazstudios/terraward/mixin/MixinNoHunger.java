package com.lunazstudios.terraward.mixin;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is an onHungerTick mixin that is called whenever a player has an update on their hunger.
 */
@Mixin(PlayerEntity.class)
public class MixinNoHunger {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;update(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        BlockPos pos = player.getBlockPos();
        MinecraftServer server = player.getServer();
        ModConfig config = ModConfigManager.getConfig(server);
        ProtectedArea area = config.findAreaByLocation(pos.getX(), pos.getY(), pos.getZ());

        if (player.getWorld().isClient) {
            return;
        }

        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        if (area != null && area.getFlagState("no-hunger")) {
            ci.cancel();
        }
    }
}