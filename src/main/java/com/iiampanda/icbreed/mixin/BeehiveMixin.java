package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.traits.HoneyDrip;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveMixin {
    @Inject(method = "addOccupant", at = @At("HEAD"))
    private void icbreed$flag(Bee bee, CallbackInfo ci) {
        Effects fx = Genetics.effects(bee);
        if (fx != null && fx.honeyBlock > 0) com.iiampanda.icbreed.platform.Att.set(((BeehiveBlockEntity) (Object) this), Genetics.HONEY_HIVE, true);
    }

    @WrapOperation(method = "addOccupant", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity$Occupant;of(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity$Occupant;"))
    private BeehiveBlockEntity.Occupant icbreed$fastHoney(Entity bee, Operation<BeehiveBlockEntity.Occupant> original) {
        BeehiveBlockEntity.Occupant o = original.call(bee);
        if (bee instanceof Bee b && o.minTicksInHive() > 600) {
            Effects fx = Genetics.effects(b);
            if (fx != null && fx.honeyFast > 0) return new BeehiveBlockEntity.Occupant(o.entityData(), o.ticksInHive(), o.minTicksInHive() / 2);
        }
        return o;
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void icbreed$drip(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity hive, CallbackInfo ci) {
        if (level.getGameTime() % 20 == 0 && Boolean.TRUE.equals(com.iiampanda.icbreed.platform.Att.get(hive, Genetics.HONEY_HIVE))) HoneyDrip.tick(level, pos, hive);
    }
}
