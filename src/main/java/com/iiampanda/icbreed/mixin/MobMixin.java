package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.player.Player;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void icbreed$passive(net.minecraft.world.entity.LivingEntity target, CallbackInfo ci) {
        if ((Object) this instanceof Animal a && target instanceof Player p
                && com.iiampanda.icbreed.traits.AttackTames.passive(a)
                && !com.iiampanda.icbreed.traits.AttackTames.allowedVictim(a, p)) {
            ci.cancel();
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void icbreed$thinSpawns(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                    net.minecraft.world.entity.MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData data, net.minecraft.nbt.CompoundTag tag,
                                    org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        if ((reason == net.minecraft.world.entity.MobSpawnType.NATURAL || reason == net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION)
                && Genetics.supports(self) && !(self instanceof net.minecraft.world.entity.ambient.Bat) && self.getRandom().nextDouble() >= com.iiampanda.icbreed.IcBreedConfig.get().naturalSpawnKeep) {
            com.iiampanda.icbreed.platform.Att.set(self, Genetics.CULL, true);
        }
    }
}
