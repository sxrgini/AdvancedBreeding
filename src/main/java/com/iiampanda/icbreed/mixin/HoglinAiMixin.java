package com.iiampanda.icbreed.mixin;

import java.util.Optional;

import com.iiampanda.icbreed.traits.AttackTames;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.hoglin.HoglinAi")
public abstract class HoglinAiMixin {
    @Inject(method = "findNearestValidAttackTarget", at = @At("RETURN"), cancellable = true)
    private static void icbreed$ignorePlayers(ServerLevel level, Hoglin hoglin,
                                              CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> found = cir.getReturnValue();
        if (found != null && found.isPresent() && found.get() instanceof Player p && AttackTames.passive(hoglin)
                && !AttackTames.allowedVictim(hoglin, p)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
