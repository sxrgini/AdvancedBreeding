package com.iiampanda.icbreed.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class NeoHooks {
    private NeoHooks() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(NeoHooks::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onServerTick);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onLevelTick);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, NeoHooks::afterDeath);
        NeoForge.EVENT_BUS.addListener(NeoHooks::afterDamage);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onLogout);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onStopping);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onJoin);
        NeoForge.EVENT_BUS.addListener(NeoHooks::onCommands);
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        for (UseEntityCallback cb : UseEntityCallback.EVENT.handlers) {
            InteractionResult r = cb.interact(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget(), null);
            if (r != InteractionResult.PASS) {
                event.setCancellationResult(r);
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        for (UseBlockCallback cb : UseBlockCallback.EVENT.handlers) {
            InteractionResult r = cb.interact(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (r != InteractionResult.PASS) {
                event.setCancellationResult(r);
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        for (UseItemCallback cb : UseItemCallback.EVENT.handlers) {
            InteractionResult r = cb.interact(event.getEntity(), event.getLevel(), event.getHand());
            if (r != InteractionResult.PASS) {
                event.setCancellationResult(r);
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        for (ServerTickEvents.EndTick cb : ServerTickEvents.END_SERVER_TICK.handlers) cb.onEndTick(event.getServer());
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (ServerTickEvents.EndLevelTick cb : ServerTickEvents.END_LEVEL_TICK.handlers) cb.onEndTick(level);
    }

    private static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        for (ServerLivingEntityEvents.AllowDeath cb : ServerLivingEntityEvents.ALLOW_DEATH.handlers) {
            if (!cb.allowDeath(entity, event.getSource(), 0.0F)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void afterDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.isCanceled()) return;
        for (ServerLivingEntityEvents.AfterDeath cb : ServerLivingEntityEvents.AFTER_DEATH.handlers) cb.afterDeath(entity, event.getSource());
    }

    private static void afterDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        for (ServerLivingEntityEvents.AfterDamage cb : ServerLivingEntityEvents.AFTER_DAMAGE.handlers) {
            cb.afterDamage(entity, event.getSource(), event.getOriginalDamage(), event.getInflictedDamage(), event.getBlockedDamage() > 0.0F);
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.connection == null) return;
        for (ServerPlayConnectionEvents.Disconnect cb : ServerPlayConnectionEvents.DISCONNECT.handlers) cb.onPlayDisconnect(player.connection, player.level().getServer());
    }

    private static void onStopping(ServerStoppingEvent event) {
        for (ServerLifecycleEvents.Stopping cb : ServerLifecycleEvents.SERVER_STOPPING.handlers) cb.onServerStopping(event.getServer());
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (ServerEntityEvents.Load cb : ServerEntityEvents.ENTITY_LOAD.handlers) cb.onLoad(event.getEntity(), level);
    }

    private static void onCommands(RegisterCommandsEvent event) {
        for (CommandRegistrationCallback cb : CommandRegistrationCallback.EVENT.handlers) {
            cb.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }
}
