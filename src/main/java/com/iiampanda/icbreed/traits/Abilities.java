package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Abilities {
    private static final long NINE_LIVES_COOLDOWN = com.iiampanda.icbreed.IcBreedConfig.get().nineLivesCooldownSeconds * 20L;

    private static final java.util.Map<java.util.UUID, Long> TOGGLED = new java.util.concurrent.ConcurrentHashMap<>();

    private Abilities() {
    }

    public static void syncSelf(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp && sp.connection != null) {
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(sp));
        }
    }

    public static void removeHat(Player player) {
        boolean any = false;
        for (Entity e : new java.util.ArrayList<>(player.getPassengers())) {
            if (e instanceof Axolotl) {
                e.stopRiding();
                any = true;
            }
        }
        if (any) syncSelf(player);
        else if (player instanceof net.minecraft.server.level.ServerPlayer sp) Shoulder.release(sp);
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer p) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
            if (!AuraManager.nineLives(p.getUUID())) return true;
            long now = p.level().getGameTime();
            Long ready = p.getAttached(Genetics.NINE_LIVES_READY);
            if (ready != null && now < ready) return true;
            p.setAttached(Genetics.NINE_LIVES_READY, now + NINE_LIVES_COOLDOWN);
            p.setHealth(1.0F);
            p.removeAllEffects();
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            p.level().broadcastEntityEvent(p, (byte) 35);
            return false;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (p.hasAttached(Genetics.CLING) && p.isShiftKeyDown() && p.horizontalCollision) p.resetFallDistance();
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handler.getPlayer().ejectPassengers());

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof net.minecraft.world.entity.Mob a) || !Genetics.supports(a) || !Genetics.isTamed(a) || !AttackTames.owns(a, player)) return InteractionResult.PASS;
            Effects fx = Genetics.effects(a);
            if (fx == null) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);

            if (a instanceof Chicken chicken && fx.feathers > 0 && held.is(Items.SHEARS) && !chicken.isBaby()) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                ServerLevel sl = (ServerLevel) level;
                long now = sl.getGameTime();
                Long ready = chicken.getAttached(Genetics.MILK_READY);
                if (ready != null && now < ready) {
                    int secs = (int) ((ready - now + 19) / 20);
                    player.displayClientMessage(Component.translatable("advbreeding.feathers.wait", String.format("%d:%02d", secs / 60, secs % 60)), true);
                    return InteractionResult.SUCCESS;
                }
                chicken.setAttached(Genetics.MILK_READY, now + IcBreedConfig.get().milkCooldownSeconds * 20L);
                chicken.spawnAtLocation(new ItemStack(Items.FEATHER, (int) fx.feathers));
                sl.playSound(null, chicken, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                held.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                Genetics.addXp(chicken, Genetics.XP_PRODUCE);
                return InteractionResult.SUCCESS;
            }

            if (a instanceof net.minecraft.world.entity.animal.horse.Llama llama && fx.llamaWool > 0 && held.is(Items.SHEARS) && !llama.isBaby()) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                ServerLevel sl = (ServerLevel) level;
                long now = sl.getGameTime();
                Long ready = llama.getAttached(Genetics.MILK_READY);
                if (ready != null && now < ready) {
                    int secs = (int) ((ready - now + 19) / 20);
                    player.displayClientMessage(Component.translatable("advbreeding.wool.wait", String.format("%d:%02d", secs / 60, secs % 60)), true);
                    return InteractionResult.SUCCESS;
                }
                llama.setAttached(Genetics.MILK_READY, now + com.iiampanda.icbreed.IcBreedConfig.get().llamaWoolCooldownSeconds * 20L);
                net.minecraft.world.item.Item wool = switch (llama.getVariant()) {
                    case BROWN -> Items.BROWN_WOOL;
                    case GRAY -> Items.GRAY_WOOL;
                    default -> Items.WHITE_WOOL;
                };
                llama.spawnAtLocation(new ItemStack(wool, (int) Math.min(3, fx.llamaWool)));
                sl.playSound(null, llama, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                held.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                Genetics.addXp(llama, Genetics.XP_PRODUCE);
                return InteractionResult.SUCCESS;
            }

            if (a instanceof Axolotl axolotl) {
                boolean worn = axolotl.getVehicle() == player;
                if (worn || (fx.hat > 0 && held.isEmpty() && player.getPassengers().stream().noneMatch(e -> e instanceof Axolotl))) {
                    if (!level.isClientSide()) {
                        long now = level.getGameTime();
                        Long last = TOGGLED.get(player.getUUID());
                        if (last == null || now - last >= 8L) {
                            TOGGLED.put(player.getUUID(), now);
                            if (worn) axolotl.stopRiding();
                            else axolotl.startRiding(player, true);
                            syncSelf(player);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            if (fx.rideable > 0 && held.isEmpty() && !a.isBaby() && !player.isPassenger() && a.getPassengers().isEmpty()) {
                if (!level.isClientSide()) {
                    StayPut.release(a);
                    player.startRiding(a);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
