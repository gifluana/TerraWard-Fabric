package com.lunazstudios.terraward.mixin;

import com.lunazstudios.terraward.area.ProtectedArea;
import com.lunazstudios.terraward.config.ModConfig;
import com.lunazstudios.terraward.config.ModConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This is an onDamage mixin that is called whenever an entity takes damage.
 */
@Mixin(LivingEntity.class)
public abstract class MixinOnDamage {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            BlockPos pos = player.getBlockPos();
            MinecraftServer server = player.getServer();
            ModConfig config = ModConfigManager.getConfig(server);
            ProtectedArea area = config.findAreaByLocation(pos.getX(), pos.getY(), pos.getZ());


            if (area != null && area.getFlagState("no-damage")) {
                cir.setReturnValue(false);
                return;
            }

            if (area != null && area.getFlagState("no-falldamage") && source.isIn(DamageTypeTags.IS_FALL)) {
                cir.setReturnValue(false);
            }
        }
    }
}