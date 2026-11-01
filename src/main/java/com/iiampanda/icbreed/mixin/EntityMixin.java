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
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean icbreed$hatOnPlayer(net.minecraft.world.entity.EntityType<?> type,
                                        com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original, Entity vehicle) {
        if ((Object) this instanceof net.minecraft.world.entity.animal.axolotl.Axolotl
                && vehicle instanceof net.minecraft.world.entity.player.Player) return true;
        return original.call(type);
    }


    @Inject(method = "getPassengerAttachmentPoint", at = @At("HEAD"), cancellable = true)
    private void icbreed$deckAttach(Entity passenger, net.minecraft.world.entity.EntityDimensions dimensions, float scale,
                                    CallbackInfoReturnable<net.minecraft.world.phys.Vec3> cir) {
        if ((Object) this instanceof net.minecraft.world.entity.animal.happyghast.HappyGhast g) {
            net.minecraft.world.phys.Vec3 v = com.iiampanda.icbreed.traits.GhastDeck.attach(g, passenger);
            if (v != null) cir.setReturnValue(v);
        }
    }

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
