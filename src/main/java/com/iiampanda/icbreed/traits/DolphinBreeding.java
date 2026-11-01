package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.genetics.Ages;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Sex;
import com.iiampanda.icbreed.taming.TamingLogic;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.item.ItemStack;

public final class DolphinBreeding {
    private static final long LOVE_TICKS = 600L;
    private static final int COOLDOWN_AGE = 6000;

    private DolphinBreeding() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof Dolphin dolphin)) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (!Ages.isFish(held)) return InteractionResult.PASS;
            boolean baby = dolphin.isBaby();
            boolean tamed = Genetics.isTamed(dolphin);
            if (!baby && !(tamed && AttackTames.owns(dolphin, player))) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ServerLevel sl = (ServerLevel) level;
            if (!baby) {
                if (Genetics.isPregnant(dolphin) || dolphin.getAge() > 0 || dolphin.getAttached(Genetics.LOVE_UNTIL) != null) {
                    return InteractionResult.SUCCESS;
                }
                dolphin.setAttached(Genetics.LOVE_UNTIL, sl.getGameTime() + LOVE_TICKS);
                sl.broadcastEntityEvent(dolphin, (byte) 18);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            sl.playSound(null, dolphin, SoundEvents.DOLPHIN_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            var fx = Genetics.effects(dolphin);
            Genetics.addXp(dolphin, (int) Math.round(Genetics.XP_FEED * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
            TamingLogic.onFed(dolphin, player.getUUID());
            return InteractionResult.SUCCESS;
        });
    }

    public static void tick(ServerLevel level, Dolphin d) {
        Long until = d.getAttached(Genetics.LOVE_UNTIL);
        if (until == null) return;
        if (level.getGameTime() > until || d.isBaby() || Genetics.isPregnant(d)) {
            d.removeAttached(Genetics.LOVE_UNTIL);
            return;
        }
        Dolphin mate = null;
        double best = Double.MAX_VALUE;
        for (Dolphin o : level.getEntitiesOfClass(Dolphin.class, d.getBoundingBox().inflate(24.0),
                o -> o != d && o.isAlive() && o.getAttached(Genetics.LOVE_UNTIL) != null && Genetics.canBreed(d, o))) {
            double dist = d.distanceToSqr(o);
            if (dist < best) {
                best = dist;
                mate = o;
            }
        }
        if (mate == null) return;
        if (best > 6.25) {
            d.getNavigation().moveTo(mate, 1.3);
            return;
        }
        Dolphin mother = Genetics.getOrCreate(d).sex == Sex.FEMALE ? d : mate;
        Dolphin father = mother == d ? mate : d;
        Genetics.conceive(mother, father);
        for (Dolphin x : new Dolphin[] {d, mate}) {
            x.removeAttached(Genetics.LOVE_UNTIL);
            x.setAge(COOLDOWN_AGE);
            level.sendParticles(ParticleTypes.HEART, x.getX(), x.getEyeY() + 0.4, x.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
