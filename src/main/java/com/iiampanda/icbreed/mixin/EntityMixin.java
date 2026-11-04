package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.NameHelper;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getCustomName", at = @At("RETURN"), cancellable = true)
    private void icbreed$sexSymbol(CallbackInfoReturnable<Component> cir) {
        Entity self = (Entity) (Object) this;
        if (NameHelper.RAW.get() || !Genetics.supports(self) || !self.level().isClientSide()
                || !IcBreedConfig.get().showSexSymbol) return;
        Genome g = self.getAttached(Genetics.GENOME);
        if (g == null) return;
        Component base = cir.getReturnValue();
        if (base == null) base = self.getType().getDescription();
        cir.setReturnValue(Component.empty().append(base)
                .append(Component.literal(" " + g.sex.symbol).withStyle(Style.EMPTY.withColor(g.sex.color))));
    }
}
