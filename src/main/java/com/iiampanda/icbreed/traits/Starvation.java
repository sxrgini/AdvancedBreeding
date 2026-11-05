package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.platform.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Starvation {
    private Starvation() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide() || player.isSpectator() || player.isShiftKeyDown()) return InteractionResult.PASS;
            if (!(entity instanceof Mob m) || !Genetics.supports(m) || !Genetics.isTamed(m)) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            if (stack.isEmpty() || !isFood(m, stack)) return InteractionResult.PASS;
            if (m.getHealth() >= m.getMaxHealth() && !hungry(m)) return InteractionResult.PASS;
            boolean bucket = stack.is(Items.TROPICAL_FISH_BUCKET);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (bucket) {
                    ItemStack back = new ItemStack(Items.WATER_BUCKET);
                    if (stack.isEmpty()) player.setItemInHand(hand, back);
                    else if (!player.getInventory().add(back)) player.drop(back, false);
                }
            }
            fed(m);
            com.iiampanda.icbreed.taming.TamingLogic.onFed(m, player.getUUID());
            m.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        });
    }

    public static boolean isFood(Mob m, ItemStack stack) {
        if (m instanceof Animal a) return a.isFood(stack);
        return stack.has(DataComponents.FOOD) || stack.is(ItemTags.FISHES);
    }

    private static long threshold(Mob m) {
        Effects fx = Genetics.effects(m);
        double stretch = fx == null ? 1.0 : fx.hungerStretch();
        return Math.max(20L, Math.round(IcBreedConfig.get().starveSeconds * 20L * stretch));
    }

    public static boolean hungry(Mob m) {
        Long last = com.iiampanda.icbreed.platform.Att.get(m, Genetics.LAST_FED);
        return last != null && m.level().getGameTime() - last >= threshold(m) / 2;
    }

    public static void fed(Mob m) {
        if (!Genetics.isTamed(m) || !(m.level() instanceof ServerLevel level)) return;
        com.iiampanda.icbreed.platform.Att.set(m, Genetics.LAST_FED, level.getGameTime());
        double heal = IcBreedConfig.get().feedHealAmount;
        if (heal > 0 && m.getHealth() < m.getMaxHealth()) {
            m.heal((float) heal);
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getY() + m.getBbHeight() * 0.8, m.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    public static void tick(Mob m) {
        if (!(m.level() instanceof ServerLevel level) || !Genetics.supports(m) || !Genetics.isTamed(m) || !m.isAlive()) return;
        long now = level.getGameTime();
        Long last = com.iiampanda.icbreed.platform.Att.get(m, Genetics.LAST_FED);
        if (last == null) {
            com.iiampanda.icbreed.platform.Att.set(m, Genetics.LAST_FED, now);
            return;
        }
        long over = now - last - threshold(m);
        if (over < 0) return;
        long interval = Math.max(20L, IcBreedConfig.get().starveIntervalSeconds * 20L);
        if (over % interval >= 20) return;
        float room = m.getHealth() - 1.0F;
        if (room <= 0) return;
        m.hurtServer(level, level.damageSources().starve(), Math.min(1.0F, room));
    }
}
