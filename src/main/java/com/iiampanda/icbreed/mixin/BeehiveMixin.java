package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.traits.HoneyDrip;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import java.util.List;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveMixin {
    @Inject(method = "addOccupantWithPresetTicks", at = @At("HEAD"))
    private void icbreed$flag(Entity bee, boolean nectar, int ticks, CallbackInfo ci) {
        if (!(bee instanceof Bee b)) return;
        Effects fx = Genetics.effects(b);
        if (fx != null && fx.honeyBlock > 0) com.iiampanda.icbreed.platform.Att.set(((BeehiveBlockEntity) (Object) this), Genetics.HONEY_HIVE, true);
    }

    @Inject(method = "addOccupantWithPresetTicks", at = @At("TAIL"))
    private void icbreed$fastHoney(Entity bee, boolean nectar, int ticks, CallbackInfo ci) {
        List<?> stored = ((BeehiveAccessor) (Object) this).icbreed$stored();
        if (!(bee instanceof Bee b) || stored.isEmpty()) return;
        BeeDataAccessor data = (BeeDataAccessor) stored.get(stored.size() - 1);
        int min = data.icbreed$getMin();
        if (min <= 600) return;
        Effects fx = Genetics.effects(b);
        if (fx != null && fx.honeyFast > 0) data.icbreed$setMin(min / 2);
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void icbreed$drip(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity hive, CallbackInfo ci) {
        if (level.getGameTime() % 20 == 0 && Boolean.TRUE.equals(com.iiampanda.icbreed.platform.Att.get(hive, Genetics.HONEY_HIVE))) HoneyDrip.tick(level, pos, hive);
    }
}
