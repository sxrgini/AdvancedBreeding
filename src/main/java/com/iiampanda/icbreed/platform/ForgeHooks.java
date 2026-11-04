package com.iiampanda.icbreed.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public final class ForgeHooks {
    private ForgeHooks() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onDeath);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, ForgeHooks::afterDeath);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::afterDamage);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onLogout);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onStopping);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onJoin);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onCommands);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(ForgeHooks::onPlayerDimension);
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

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerTickEvents.EndTick cb : ServerTickEvents.END_SERVER_TICK.handlers) cb.onEndTick(event.getServer());
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        for (ServerTickEvents.EndWorldTick cb : ServerTickEvents.END_WORLD_TICK.handlers) cb.onEndTick(level);
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

    private static void afterDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.isCanceled()) return;
        for (ServerLivingEntityEvents.AfterDamage cb : ServerLivingEntityEvents.AFTER_DAMAGE.handlers) {
            cb.afterDamage(entity, event.getSource(), event.getAmount(), event.getAmount(), false);
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.connection == null) return;
        for (ServerPlayConnectionEvents.Disconnect cb : ServerPlayConnectionEvents.DISCONNECT.handlers) cb.onPlayDisconnect(player.connection, player.getServer());
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

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer) AttSync.sendTracked(viewer, event.getTarget());
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) AttSync.sendTracked(p, p);
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) AttSync.sendTracked(p, p);
    }

    private static void onPlayerDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) AttSync.sendTracked(p, p);
    }
}
