package com.iiampanda.icbreed;

import com.iiampanda.icbreed.block.ModBlocks;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Taming;
import com.iiampanda.icbreed.net.RenamePayload;
import com.iiampanda.icbreed.taming.TamingLogic;
import com.iiampanda.icbreed.traits.TraitActions;
import com.iiampanda.icbreed.traits.AuraManager;
import com.iiampanda.icbreed.net.UpgradeTraitPayload;
import com.iiampanda.icbreed.net.OpenBagsPayload;
import com.iiampanda.icbreed.traits.SaddleBags;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.iiampanda.icbreed.genetics.Pregnancy;
import com.iiampanda.icbreed.net.OpenGeneticsPayload;
import com.iiampanda.icbreed.net.OpenInventoryPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcBreed implements ModInitializer {
    public static final String MOD_ID = "advbreeding";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        IcBreedConfig.load();
        Genetics.init();
        ModBlocks.init();
        com.iiampanda.icbreed.traits.Starvation.register();
        com.iiampanda.icbreed.traits.Shoulder.register();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> IcBreedCommand.register(dispatcher));

        PayloadTypeRegistry.clientboundPlay().register(OpenGeneticsPayload.TYPE, OpenGeneticsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(com.iiampanda.icbreed.net.RulesSyncPayload.TYPE, com.iiampanda.icbreed.net.RulesSyncPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(com.iiampanda.icbreed.config.Rules::tick);
        PayloadTypeRegistry.clientboundPlay().register(com.iiampanda.icbreed.net.StayStatePayload.TYPE, com.iiampanda.icbreed.net.StayStatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OpenInventoryPayload.TYPE, OpenInventoryPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RenamePayload.TYPE, RenamePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpgradeTraitPayload.TYPE, UpgradeTraitPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OpenBagsPayload.TYPE, OpenBagsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(com.iiampanda.icbreed.net.StayPutPayload.TYPE, com.iiampanda.icbreed.net.StayPutPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(com.iiampanda.icbreed.net.StayPutPayload.TYPE, (payload, context) -> {
            var player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof net.minecraft.world.entity.Mob mob
                    && player.distanceToSqr(mob) < 32.0 * 32.0 && com.iiampanda.icbreed.traits.StayPut.can(mob, player)) {
                if (!com.iiampanda.icbreed.traits.StayPut.isOwner(mob, player)) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("advbreeding.sit.not_owner"));
                    return;
                }
                var mode = com.iiampanda.icbreed.traits.StayPut.cycle(mob, player);
                player.sendOverlayMessage(com.iiampanda.icbreed.traits.StayPut.message(mob, mode));
                ServerPlayNetworking.send((ServerPlayer) player, new com.iiampanda.icbreed.net.StayStatePayload(mob.getId(), mode.ordinal(), true));
            }
        });
        PayloadTypeRegistry.serverboundPlay().register(com.iiampanda.icbreed.net.RideAttackPayload.TYPE, com.iiampanda.icbreed.net.RideAttackPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(com.iiampanda.icbreed.net.RideAttackPayload.TYPE, (payload, context) ->
                com.iiampanda.icbreed.traits.RideAttack.attack(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(com.iiampanda.icbreed.net.RemoveHatPayload.TYPE, com.iiampanda.icbreed.net.RemoveHatPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(com.iiampanda.icbreed.net.RemoveHatPayload.TYPE, (payload, context) ->
                com.iiampanda.icbreed.traits.Abilities.removeHat(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(OpenBagsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof AbstractHorse horse && player.distanceToSqr(horse) <= 64) {
                SaddleBags.open(player, horse);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(UpgradeTraitPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof net.minecraft.world.entity.Mob animal && Genetics.supports(animal) && player.distanceToSqr(animal) <= 64) {
                Genetics.tryUpgrade(animal, payload.traitId());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!(player.level().getEntity(payload.entityId()) instanceof net.minecraft.world.entity.Mob animal) || !Genetics.supports(animal)) return;
            if (player.distanceToSqr(animal) > 64 || Genetics.isWild(animal)) return;
            String name = payload.name().strip();
            if (name.length() > 32) name = name.substring(0, 32);
            animal.setCustomName(name.isEmpty() ? null : Component.literal(name));
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!level.isClientSide() && entity instanceof AbstractHorse horse && horse.isBaby()
                    && horse.isFood(player.getItemInHand(hand))) {
                TamingLogic.onFed(horse, player.getUUID());
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!(entity instanceof AbstractHorse horse)) return InteractionResult.PASS;
            int style = SaddleBags.styleOf(player.getItemInHand(hand));
            if (!SaddleBags.canFit(horse, style)) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                SaddleBags.fit(horse, style, player.getItemInHand(hand));
                if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.SUCCESS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof net.minecraft.world.entity.animal.cow.AbstractCow cow) || cow.isBaby()) return InteractionResult.PASS;
            if (!player.getItemInHand(hand).is(net.minecraft.world.item.Items.BUCKET)) return InteractionResult.PASS;
            long now = level.getGameTime();
            Long ready = cow.getAttached(Genetics.MILK_READY);
            if (ready != null && ready > now) {
                int secs = (int) ((ready - now + 19) / 20);
                player.sendOverlayMessage(Component.translatable(cow instanceof net.minecraft.world.entity.animal.cow.MushroomCow ? "advbreeding.msg.stew_wait" : "advbreeding.msg.milk_wait",
                        String.format("%d:%02d", secs / 60, secs % 60)));
                return InteractionResult.FAIL;
            }
            if (level.isClientSide()) return InteractionResult.PASS;
            Effects fx = Genetics.effects(cow);
            int rank = fx == null ? 0 : (int) Math.min(3, fx.productionRank);
            long cooldown = Math.max(IcBreedConfig.get().milkMinimumSeconds * 20L, IcBreedConfig.get().milkCooldownSeconds * 20L - rank * IcBreedConfig.get().milkReductionSecondsPerRank * 20L);
            cow.setAttached(Genetics.MILK_READY, now + cooldown);
            Genetics.addXp(cow, Genetics.XP_PRODUCE);
            return InteractionResult.PASS;
        });

        ServerPlayNetworking.registerGlobalReceiver(OpenInventoryPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            Entity e = player.level().getEntity(payload.entityId());
            if (e instanceof AbstractHorse horse && horse.isTamed() && player.distanceToSqr(horse) < 64) {
                horse.openCustomInventoryScreen(player);
            }
        });

        com.iiampanda.icbreed.traits.Abilities.register();
        com.iiampanda.icbreed.traits.MooshroomStew.register();
        com.iiampanda.icbreed.traits.DolphinBreeding.register();
        com.iiampanda.icbreed.traits.GhastDeck.register();

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            com.iiampanda.icbreed.config.Rules.ensureLoaded(world.getServer());
            if (entity instanceof net.minecraft.server.level.ServerPlayer joined) {
                com.iiampanda.icbreed.config.Rules.sendTo(joined);
                return;
            }
            if (entity instanceof net.minecraft.world.entity.Mob mob && mob.hasAttached(Genetics.CULL) && !mob.isLeashed() && !mob.hasCustomName()) {
                world.getServer().execute(mob::discard);
                return;
            }
            if (entity instanceof net.minecraft.world.entity.Mob mob && Genetics.supports(mob)) Genetics.onLoad(mob);
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || player.isSpectator()) {
                return InteractionResult.PASS;
            }
            if (!(entity instanceof net.minecraft.world.entity.Mob animal) || !Genetics.supports(animal)) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (com.iiampanda.icbreed.genetics.Ages.isFood(animal, held)) return InteractionResult.PASS;
            if (animal instanceof AbstractHorse bagged && held.is(net.minecraft.world.item.Items.SHEARS)
                    && bagged.getAttached(Genetics.BAGS) != null) {
                if (!level.isClientSide()) SaddleBags.remove(bagged, player, hand);
                return InteractionResult.SUCCESS;
            }
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer sp) {
                Genome g = Genetics.getOrCreate(animal);
                boolean horse = animal instanceof AbstractHorse h && h.isTamed();
                Pregnancy pregnancy = animal.getAttached(Genetics.PREGNANCY);
                ServerPlayNetworking.send(sp, new OpenGeneticsPayload(animal.getId(), g, horse,
                        pregnancy == null ? -1 : (int) pregnancy.ticksLeft(animal.level().getGameTime())));
                ServerPlayNetworking.send(sp, new com.iiampanda.icbreed.net.StayStatePayload(animal.getId(), com.iiampanda.icbreed.traits.StayPut.mode(animal).ordinal(), com.iiampanda.icbreed.traits.StayPut.isOwner(animal, sp)));
            }
            return InteractionResult.SUCCESS;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof net.minecraft.world.entity.Mob dead && Genetics.supports(dead) && !dead.level().isClientSide()) TamingLogic.onAdultDied(dead);
            if (entity instanceof AbstractHorse horse && !horse.level().isClientSide()) SaddleBags.dropAll(horse);

            if (source.getEntity() instanceof net.minecraft.world.entity.Mob killer && Genetics.supports(killer) && !killer.level().isClientSide() && entity != killer) {
                Genetics.addXp(killer, Genetics.XP_KILL);
            }
        });

        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || player.isSpectator()
                    || !player.getMainHandItem().isEmpty()) {
                return InteractionResult.PASS;
            }
            net.minecraft.core.BlockPos pos = hit.getBlockPos();
            if (!level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.HAY_BLOCK)) return InteractionResult.PASS;
            for (int ox = -1; ox <= 0; ox++) {
                for (int oz = -1; oz <= 0; oz++) {
                    int x = pos.getX() + ox, z = pos.getZ() + oz;
                    if (!TamingLogic.square(level, x, pos.getY(), z, net.minecraft.world.level.block.Blocks.HAY_BLOCK)) continue;
                    if (level.isClientSide()) return InteractionResult.SUCCESS;
                    for (int dx = 0; dx <= 1; dx++) {
                        for (int dz = 0; dz <= 1; dz++) {
                            level.setBlock(new net.minecraft.core.BlockPos(x + dx, pos.getY(), z + dz),
                                    ModBlocks.HAY_CARPET.defaultBlockState(), 3);
                        }
                    }
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GRASS_PLACE,
                            net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.9F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            drops.removeIf(s -> s.is(net.minecraft.world.item.Items.NAME_TAG)
                    && ctx.getRandom().nextDouble() >= IcBreedConfig.get().nameTagLootChance);
            if (ctx.getOptional(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY) instanceof net.minecraft.world.entity.Mob dead && Genetics.supports(dead)) {
                Effects dfx = Genetics.effects(dead);
                if (dfx != null) {
                    TraitActions.extraDrops(dead, dfx, drops);
                    if (dead.isAlive() && dead instanceof net.minecraft.world.entity.animal.sheep.Sheep) {
                        for (ItemStack s : drops) {
                            if (s.is(net.minecraft.tags.ItemTags.WOOL)) {
                                Genetics.addXp(dead, Genetics.XP_PRODUCE);
                                break;
                            }
                        }
                    }

                    if (dead.isAlive() && dfx.productionRank > 0 && dead instanceof net.minecraft.world.entity.animal.sheep.Sheep
                            && ctx.getRandom().nextDouble() < IcBreedConfig.get().productionChancePerRank * Math.min(3, dfx.productionRank)) {
                        var extra = new java.util.ArrayList<ItemStack>();
                        for (ItemStack s : drops) if (s.is(net.minecraft.tags.ItemTags.WOOL)) extra.add(s.copy());
                        drops.addAll(extra);
                    }
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0 || !(source.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                    || player.level().isClientSide()) return;
            if (!(player.getVehicle() instanceof AbstractHorse mount) || !player.getMainHandItem().is(net.minecraft.tags.ItemTags.SPEARS)) return;
            Effects mfx = Genetics.effects(mount);
            if (mfx == null || mfx.unseatChance <= 0 || player.getRandom().nextDouble() >= mfx.unseatChance) return;
            if (victim instanceof AbstractHorse) victim.ejectPassengers();
            else if (victim.getVehicle() instanceof AbstractHorse) victim.stopRiding();
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0 || !(source.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
            Effects aura = AuraManager.hitAura(player.getUUID());
            if (aura == null) return;
            TraitActions.onHit(player, victim, aura);
            if (aura.leechChance > 0 && player.getRandom().nextDouble() < aura.leechChance) player.heal(damageTaken);
            if (aura.bleedChance > 0 && player.getRandom().nextDouble() < aura.bleedChance) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), player);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0 || victim.level().isClientSide()) return;
            if (victim instanceof ServerPlayer owner && source.getEntity() instanceof net.minecraft.world.entity.LivingEntity foe) {
                com.iiampanda.icbreed.traits.AttackTames.defend(owner, foe);
            }
            if (source.getEntity() instanceof ServerPlayer owner) {
                com.iiampanda.icbreed.traits.AttackTames.assist(owner, victim);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0) return;
            if (victim instanceof net.minecraft.world.entity.Mob hurt && Genetics.supports(hurt) && source.getEntity() != null && !hurt.level().isClientSide()) {
                Genetics.onPregnantHit(hurt);
                Effects hfx = Genetics.effects(hurt);
                if (hfx != null) TraitActions.onHurt(hurt, hfx);
            }
            if (!(source.getEntity() instanceof net.minecraft.world.entity.Mob attacker) || !Genetics.supports(attacker) || attacker.level().isClientSide()) return;
            Effects fx = Genetics.effects(attacker);
            if (fx == null) return;
            TraitActions.onHit(attacker, victim, fx);
            if (fx.leechChance > 0 && attacker.getRandom().nextDouble() < fx.leechChance) {
                attacker.heal(damageTaken);
            }
            if (fx.bleedChance > 0 && attacker.getRandom().nextDouble() < fx.bleedChance) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), attacker);
            }
        });

        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            if (level.getGameTime() % 20 != 0) return;

            java.util.List<net.minecraft.world.entity.Mob> animals = new java.util.ArrayList<>();
            for (Entity e : level.getAllEntities()) {
                if (e instanceof net.minecraft.world.entity.Mob a && Genetics.supports(a)) animals.add(a);
            }
            AuraManager.update(level, animals);
            for (net.minecraft.world.entity.Mob animal : animals) {
                if (!animal.isAlive()) continue;
                Genome g = animal.getAttached(Genetics.GENOME);
                if (g == null) continue;
                com.iiampanda.icbreed.genetics.Taming tm = animal.getAttached(Genetics.TAMING);
                if (tm != null && (tm.tamed() || tm.started() || tm.orphan() || tm.young() || com.iiampanda.icbreed.genetics.Ages.isBaby(animal) || animal.isLeashed())) animal.setPersistenceRequired();
                Effects fx = Genetics.effects(animal);
                TraitActions.upkeep(level, animal, fx);
                com.iiampanda.icbreed.traits.StayPut.followUpkeep(animal);
                if (animal instanceof net.minecraft.world.entity.animal.dolphin.Dolphin dolphin) com.iiampanda.icbreed.traits.DolphinBreeding.tick(level, dolphin);
                com.iiampanda.icbreed.traits.AttackTames.tick(animal, level);
                Genetics.tickPregnancy(level, animal);
                boolean grows = TamingLogic.tick(level, animal);
                Genetics.updateGrowth(animal);
                if (!grows) continue;
                int age = com.iiampanda.icbreed.genetics.Ages.getAge(animal);
                if (age < 0 && fx.growthPct != 0) {
                    int extra = (int) Math.round(20 * Genetics.clamp(fx.growthPct, -0.9, 5));
                    com.iiampanda.icbreed.genetics.Ages.setAge(animal, Math.min(0, age + extra));
                } else if (age > 0 && fx.fertilityPct != 0) {
                    int extra = (int) Math.round(20 * Genetics.clamp(fx.fertilityPct, -0.9, 5));
                    com.iiampanda.icbreed.genetics.Ages.setAge(animal, Math.max(0, age - extra));
                }
                if (animal instanceof Chicken chicken && !chicken.isBaby() && fx.eggPct != 0) {
                    int extra = (int) Math.round(20 * Genetics.clamp(fx.eggPct, -0.9, 5));
                    chicken.eggTime = Math.max(1, chicken.eggTime - extra);
                }
            }
        });

        LOGGER.info("Advanced Breeding loaded: animals now have a sex, bloodline and genotypes. Shift + right-click one to inspect it.");
    }
}
