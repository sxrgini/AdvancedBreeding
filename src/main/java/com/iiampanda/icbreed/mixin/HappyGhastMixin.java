package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.GhastDeck;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.Level;
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
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private boolean icbreed$quietAdd(Level level, Entity source, double x, double y, double z, SoundEvent sound, SoundSource category,
                                     float volume, float pitch, Entity passenger) {
        return !GhastDeck.isDeckPart(passenger);
    }

    @WrapWithCondition(method = "removePassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private boolean icbreed$quietRemove(Level level, Entity source, double x, double y, double z, SoundEvent sound, SoundSource category,
                                        float volume, float pitch, Entity passenger) {
        return !GhastDeck.isDeckPart(passenger);
    }
}
