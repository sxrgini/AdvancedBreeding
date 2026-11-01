package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private Effects icbreed$effects() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof net.minecraft.world.entity.Mob mob) || !Genetics.supports(mob) || self.level().isClientSide()) return null;
        return Genetics.effects(mob);
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float icbreed$scaleDamage(float amount, ServerLevel level, DamageSource source) {
        Effects fx = icbreed$effects();
        if (fx == null) return amount;
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)) {
            return amount * (float) Genetics.clamp(1.0 + fx.elementTakenPct, 0.1, 3.0);
        }
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            double factor = 1.0 + fx.damageTakenPct - fx.physRes;
            return amount * (float) Genetics.clamp(factor, 0.1, 3.0);
        }
        return amount;
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float icbreed$scaleHeal(float amount) {
        Effects fx = icbreed$effects();
        if (fx == null || fx.regenPct == 0) return amount;
        return amount * (float) Math.max(0.0, 1.0 + fx.regenPct);
    }

    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void icbreed$threat(ServerLevel level, @Nullable Entity entity, CallbackInfoReturnable<Double> cir) {
        if (!((Object) this instanceof net.minecraft.server.level.ServerPlayer player)) return;
        double t = com.iiampanda.icbreed.traits.AuraManager.threat(player.getUUID());
        if (t == 0) return;
        cir.setReturnValue(cir.getReturnValueD() * Genetics.clamp(1.0 + t, 0.05, 3.0));
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void icbreed$passiveTames(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof net.minecraft.world.entity.player.Player victim
                && source.getEntity() instanceof net.minecraft.world.entity.animal.Animal attacker
                && com.iiampanda.icbreed.traits.AttackTames.passive(attacker)
                && !com.iiampanda.icbreed.traits.AttackTames.allowedVictim(attacker, victim)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRiddenInput", at = @At("RETURN"), cancellable = true)
    private void icbreed$rideInput(net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.Vec3 input,
                                   CallbackInfoReturnable<net.minecraft.world.phys.Vec3> cir) {
        if (!com.iiampanda.icbreed.traits.Rideable.is((net.minecraft.world.entity.Entity) (Object) this)) return;
        float z = player.zza < 0.0F ? player.zza * 0.25F : player.zza;
        cir.setReturnValue(new net.minecraft.world.phys.Vec3(player.xxa * 0.5F, 0.0, z));
    }

    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    private void icbreed$rideSpeed(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (com.iiampanda.icbreed.traits.Rideable.is(self)) {
            cir.setReturnValue((float) self.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));
        }
    }

    @Inject(method = "tickRidden", at = @At("HEAD"))
    private void icbreed$rideTurn(net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!com.iiampanda.icbreed.traits.Rideable.is(self)) return;
        self.setYRot(player.getYRot());
        self.setXRot(player.getXRot() * 0.5F);
        self.yRotO = self.yBodyRot = self.yHeadRot = self.getYRot();
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V"))
    private void icbreed$stayTravel(LivingEntity self, net.minecraft.world.phys.Vec3 input,
                                    com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (com.iiampanda.icbreed.traits.StayPut.holding(self)) original.call(self, net.minecraft.world.phys.Vec3.ZERO);
        else original.call(self, input);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void icbreed$stayJump(CallbackInfo ci) {
        if (com.iiampanda.icbreed.traits.StayPut.holding((LivingEntity) (Object) this)) ci.cancel();
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$lavaWalker(CallbackInfo ci) {
        if (!((Object) this instanceof net.minecraft.world.entity.monster.Strider s) || s.level().isClientSide()) return;
        var attr = s.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("advbreeding", "lava_speed");
        Effects fx = icbreed$effects();
        double want = fx != null && s.isInLava() ? fx.lavaPct : 0.0;
        var cur = attr.getModifier(id);
        if (cur == null ? want == 0.0 : cur.amount() == want) return;
        attr.removeModifier(id);
        if (want != 0.0) {
            attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, want,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$desertWalker(CallbackInfo ci) {
        if (!((Object) this instanceof net.minecraft.world.entity.animal.camel.Camel c) || c.level().isClientSide()) return;
        var attr = c.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("advbreeding", "sand_speed");
        Effects fx = icbreed$effects();
        boolean sand = c.level().getBlockState(c.getOnPos()).is(net.minecraft.tags.BlockTags.SAND);
        double want = fx != null && sand ? fx.sandPct : 0.0;
        var cur = attr.getModifier(id);
        if (cur == null ? want == 0.0 : cur.amount() == want) return;
        attr.removeModifier(id);
        if (want != 0.0) {
            attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, want,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$stay(CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.Mob m && !m.level().isClientSide()
                && (m.hasAttached(Genetics.SITTING) || m instanceof net.minecraft.world.entity.TamableAnimal)) {
            com.iiampanda.icbreed.traits.StayPut.enforce(m);
        }
    }

    @Inject(method = "tickRidden", at = @At("TAIL"))
    private void icbreed$mountJump(net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.Vec3 input, CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.Mob m && com.iiampanda.icbreed.traits.MountJump.species(m)) {
            com.iiampanda.icbreed.traits.MountJump.tick(m);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$elytraBoost(CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.player.Player p && p.isFallFlying()) {
            Integer perks = p.getAttached(Genetics.PERKS);
            if (perks != null && (perks & 2) != 0) p.setDeltaMovement(p.getDeltaMovement().scale(1.0009));
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$starve(CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.Mob m && !m.level().isClientSide() && m.tickCount % 20 == 0
                && m.hasAttached(Genetics.TAMING)) {
            com.iiampanda.icbreed.traits.Starvation.tick(m);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$ghastDeck(CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.animal.happyghast.HappyGhast g && !g.level().isClientSide()) {
            com.iiampanda.icbreed.traits.GhastDeck.tick(g);
        }
    }
}
