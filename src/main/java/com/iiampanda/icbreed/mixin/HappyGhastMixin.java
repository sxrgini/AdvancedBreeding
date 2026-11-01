package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.GhastDeck;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public abstract class HappyGhastMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int icbreed$longerStill(int original) {
        return 80;
    }

    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void icbreed$noHarness(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (slot == EquipmentSlot.BODY && GhastDeck.hasBlocks((HappyGhast) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "canDispenserEquipIntoSlot", at = @At("HEAD"), cancellable = true)
    private void icbreed$noDispenserHarness(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (slot == EquipmentSlot.BODY && GhastDeck.hasBlocks((HappyGhast) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void icbreed$seat(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() != null) return;
        LivingEntity rider = GhastDeck.seated((HappyGhast) (Object) this);
        if (rider != null) cir.setReturnValue(rider);
    }

    @WrapWithCondition(method = "addPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/happyghast/HappyGhast;playSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private boolean icbreed$quietAdd(HappyGhast self, SoundEvent sound, Entity passenger) {
        return !GhastDeck.isDeckPart(passenger);
    }

    @WrapWithCondition(method = "removePassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/happyghast/HappyGhast;playSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private boolean icbreed$quietRemove(HappyGhast self, SoundEvent sound, Entity passenger) {
        return !GhastDeck.isDeckPart(passenger);
    }
}
