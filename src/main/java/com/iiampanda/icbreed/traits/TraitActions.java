package com.iiampanda.icbreed.traits;

import java.util.List;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Taming;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.phys.AABB;

public final class TraitActions {
    private static final ResourceLocation COORD_ID = ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "coordinated");

    private TraitActions() {
    }

    public static void upkeep(ServerLevel level, net.minecraft.world.entity.Mob animal, Effects fx) {
        if (animal instanceof net.minecraft.world.entity.animal.axolotl.Axolotl ax && ax.getVehicle() instanceof net.minecraft.world.entity.player.Player wearer) {
            if (wearer.level() != ax.level() || !wearer.isAlive()) ax.stopRiding();
            else {
                ax.setAirSupply(ax.getMaxAirSupply());
                Abilities.syncSelf(wearer);
            }
        }

        if ((fx.waterBreath > 0 || fx.waterVision > 0) && animal.getControllingPassenger() instanceof net.minecraft.world.entity.player.Player rider) {
            if (fx.waterBreath > 0) {
                rider.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 100, 0, true, false, true));
            }
            if (fx.waterVision > 0 && rider.isUnderWater()) {
                rider.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 240, 0, true, false, true));
            }
        }

        if (fx.regenPerSec > 0 && animal.getHealth() < animal.getMaxHealth()) {
            animal.heal((float) fx.regenPerSec);
        }

        if (animal instanceof Sheep sheep && sheep.isSheared() && fx.growthPct > 0
                && animal.getRandom().nextDouble() < Math.min(0.5, fx.growthPct * 0.02)) {
            sheep.setSheared(false);
        }

        AttributeInstance dmg = animal.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.removeModifier(COORD_ID);
            if (fx.coordPct > 0) {
                int allies = 0;
                AABB box = animal.getBoundingBox().inflate(12.0);
                for (net.minecraft.world.entity.Mob other : level.getEntitiesOfClass(animal.getClass(), box,
                        a -> a != animal && a.isAlive() && !a.isBaby() && !Genetics.isWild(a))) {
                    if (++allies >= 3) break;
                }
                if (allies > 0) {
                    dmg.addTransientModifier(new AttributeModifier(COORD_ID, fx.coordPct * allies,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
    }

    public static void onHit(LivingEntity attacker, LivingEntity victim, Effects fx) {
        var rnd = attacker.getRandom();
        if (fx.poisonBiteSeconds > 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (fx.poisonBiteSeconds * 20), 0), attacker);
        }
        if (fx.markSeconds > 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, (int) (fx.markSeconds * 20), 0), attacker);
        }
        if (fx.stunChance > 0 && rnd.nextDouble() < fx.stunChance) {
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 6), attacker);
        }
    }

    public static void onHurt(net.minecraft.world.entity.Mob victim, Effects fx) {
        if (fx.chargeSeconds > 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) (fx.chargeSeconds * 20), 1));
        }
    }

    public static void extraDrops(net.minecraft.world.entity.Mob dead, Effects fx, List<net.minecraft.world.item.ItemStack> drops) {
        if (fx.extraDropChance <= 0) return;
        var extra = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        for (var s : drops) {
            if (s.has(net.minecraft.core.component.DataComponents.FOOD) && dead.getRandom().nextDouble() < fx.extraDropChance) {
                extra.add(s.copy());
            }
        }
        drops.addAll(extra);
    }
}
