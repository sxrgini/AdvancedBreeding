package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Ages;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Sex;
import com.iiampanda.icbreed.platform.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class BatBreeding {
    private static final Map<Bat, UUID> MATES = new WeakHashMap<>();

    private BatBreeding() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof Bat bat)) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (!Ages.isBatFood(held)) return InteractionResult.PASS;
            boolean baby = bat.isBaby();
            boolean tamed = Genetics.isTamed(bat);
            if (!baby && !(tamed && AttackTames.owns(bat, player))) return InteractionResult.PASS;
            if (tamed && !baby && (bat.getHealth() < bat.getMaxHealth() || Starvation.hungry(bat))) return InteractionResult.PASS;
            if (baby && tamed && com.iiampanda.icbreed.taming.TamingLogic.saturated(bat)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ServerLevel sl = (ServerLevel) level;
            if (!baby) {
                if (Genetics.isPregnant(bat) || Ages.getAge(bat) > 0 || com.iiampanda.icbreed.platform.Att.get(bat, Genetics.LOVE_UNTIL) != null) {
                    return InteractionResult.SUCCESS;
                }
                com.iiampanda.icbreed.platform.Att.set(bat, Genetics.LOVE_UNTIL, sl.getGameTime() + IcBreedConfig.get().batLoveSeconds * 20L);
                hearts(sl, bat);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            bat.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.2F);
            Genetics.fed(bat, player.getUUID());
            return InteractionResult.SUCCESS;
        });
    }

    private static void hearts(ServerLevel level, Bat b) {
        level.sendParticles(ParticleTypes.HEART, b.getX(), b.getEyeY() + 0.3, b.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
    }

    public static void ageTick(Bat bat) {
        int age = Ages.getAge(bat);
        if (age < 0) Ages.setAge(bat, Math.min(0, age + 20));
        else if (age > 0) Ages.setAge(bat, Math.max(0, age - 20));
    }

    public static Bat mate(ServerLevel level, Bat bat) {
        UUID id = MATES.get(bat);
        if (id == null) return null;
        Entity e = level.getEntity(id);
        return e instanceof Bat b && b.isAlive() ? b : null;
    }

    public static void tick(ServerLevel level, Bat bat) {
        Long until = com.iiampanda.icbreed.platform.Att.get(bat, Genetics.LOVE_UNTIL);
        if (until == null) {
            MATES.remove(bat);
            return;
        }
        if (level.getGameTime() > until || bat.isBaby() || Genetics.isPregnant(bat)) {
            com.iiampanda.icbreed.platform.Att.remove(bat, Genetics.LOVE_UNTIL);
            MATES.remove(bat);
            return;
        }
        Bat mate = null;
        double best = Double.MAX_VALUE;
        for (Bat o : level.getEntitiesOfClass(Bat.class, bat.getBoundingBox().inflate(24.0),
                o -> o != bat && o.isAlive() && !o.isBaby() && com.iiampanda.icbreed.platform.Att.get(o, Genetics.LOVE_UNTIL) != null && Genetics.canBreed(bat, o))) {
            double dist = bat.distanceToSqr(o);
            if (dist < best) {
                best = dist;
                mate = o;
            }
        }
        if (mate == null) {
            MATES.remove(bat);
            return;
        }
        MATES.put(bat, mate.getUUID());
        if (best > 6.25) return;
        Bat mother = Genetics.getOrCreate(bat).sex == Sex.FEMALE ? bat : mate;
        Bat father = mother == bat ? mate : bat;
        Genetics.conceive(mother, father);
        int cooldown = IcBreedConfig.get().batBreedCooldownSeconds * 20;
        for (Bat x : new Bat[] {bat, mate}) {
            com.iiampanda.icbreed.platform.Att.remove(x, Genetics.LOVE_UNTIL);
            MATES.remove(x);
            Ages.setAge(x, cooldown);
            hearts(level, x);
        }
    }

    public static Bat newBaby(ServerLevel level) {
        Bat baby = net.minecraft.world.entity.EntityType.BAT.create(level);
        if (baby == null) return null;
        Ages.setAge(baby, AgeableMob.BABY_START_AGE);
        baby.setResting(false);
        return baby;
    }
}
