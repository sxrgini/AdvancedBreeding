package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.mixin.ServerPlayerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Shoulder {
    public static final ThreadLocal<Boolean> FORCE = ThreadLocal.withInitial(() -> false);

    private static final Map<UUID, Mission> MISSIONS = new HashMap<>();
    private static final Map<UUID, Long> NEXT = new HashMap<>();

    private static final class Mission {
        UUID parrot;
        UUID item;
        ItemStack carry = ItemStack.EMPTY;
        boolean returning;
        int ticks;
    }

    private Shoulder() {
    }

    public static void register() {
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != net.minecraft.world.InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) return net.minecraft.world.InteractionResult.PASS;
            if (!(entity instanceof Parrot parrot) || !player.getMainHandItem().isEmpty() || !parrot.isTame() || !parrot.isOwnedBy(player)) return net.minecraft.world.InteractionResult.PASS;
            if (parrot.getAttachedOrElse(Genetics.TRAITS, com.iiampanda.icbreed.genetics.Traits.NONE).rank("shoulder_pal") <= 0) return net.minecraft.world.InteractionResult.PASS;
            if (level.isClientSide()) return net.minecraft.world.InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer sp && MISSIONS.get(sp.getUUID()) == null) {
                StayPut.release(parrot);
                StayPut.sit(parrot, false);
                parrot.setEntityOnShoulder(sp);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) tick(p);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (UUID id : new ArrayList<>(MISSIONS.keySet())) {
                ServerPlayer p = server.getPlayerList().getPlayer(id);
                Mission m = MISSIONS.remove(id);
                if (m != null) finish(server, p, m);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer p = handler.getPlayer();
            Mission m = MISSIONS.remove(p.getUUID());
            if (m != null) finish(server, p, m);
            NEXT.remove(p.getUUID());
        });
    }

    private static boolean isParrot(CompoundTag t) {
        return t != null && !t.isEmpty() && "minecraft:parrot".equals(t.getStringOr("id", ""));
    }

    private static int rank(CompoundTag t, String id) {
        return t.getCompound("fabric:attachments")
                .flatMap(a -> a.getCompound("advbreeding:traits"))
                .map(c -> c.getIntOr(id, 0)).orElse(0);
    }

    public static int perks(ServerPlayer p) {
        int v = 0;
        for (CompoundTag t : new CompoundTag[] {p.getShoulderEntityLeft(), p.getShoulderEntityRight()}) {
            if (!isParrot(t) || rank(t, "shoulder_pal") <= 0) continue;
            v |= 1;
            if (rank(t, "elytra_boost") > 0) v |= 2;
            v |= Math.min(2, rank(t, "polly_pickup")) << 2;
        }
        return v;
    }

    public static void refresh(ServerPlayer p) {
        int v = perks(p);
        Integer cur = p.getAttached(Genetics.PERKS);
        if (v == 0) {
            if (cur != null) p.removeAttached(Genetics.PERKS);
        } else if (cur == null || cur != v) {
            p.setAttached(Genetics.PERKS, v);
        }
    }

    public static void release(ServerPlayer p) {
        Integer pk = p.getAttached(Genetics.PERKS);
        if (pk == null || (pk & 1) == 0 || MISSIONS.containsKey(p.getUUID())) return;
        FORCE.set(true);
        try {
            ((ServerPlayerAccessor) p).icbreed$removeShoulder();
        } finally {
            FORCE.set(false);
        }
        p.removeAttached(Genetics.PERKS);
        for (Parrot parrot : p.level().getEntitiesOfClass(Parrot.class, p.getBoundingBox().inflate(2.0), x -> x.isOwnedBy(p))) {
            StayPut.sit(parrot, true);
        }
    }

    private static void tick(ServerPlayer p) {
        Mission m = MISSIONS.get(p.getUUID());
        if (m != null) {
            mission(p, m);
            return;
        }
        if (p.tickCount % 10 == 0) refresh(p);
        if (p.tickCount % 10 == 5) launch(p);
    }

    private static boolean canTake(ServerPlayer p, ItemStack stack) {
        var inv = p.getInventory();
        return inv.getFreeSlot() >= 0 || inv.getSlotWithRemainingSpace(stack) >= 0;
    }

    private static void launch(ServerPlayer p) {
        Integer pk = p.getAttached(Genetics.PERKS);
        int rank = pk == null ? 0 : (pk >> 2) & 3;
        if (rank <= 0 || !p.isAlive() || p.isSpectator()) return;
        Long next = NEXT.get(p.getUUID());
        ServerLevel level = p.level();
        if (next != null && level.getGameTime() < next) return;
        CompoundTag left = p.getShoulderEntityLeft();
        boolean useLeft = isParrot(left) && rank(left, "polly_pickup") > 0;
        CompoundTag tag = useLeft ? left : p.getShoulderEntityRight();
        if (!isParrot(tag) || rank(tag, "polly_pickup") <= 0) return;
        double r = 5.0 * rank;
        ItemEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(r),
                i -> i.isAlive() && !i.hasPickUpDelay() && canTake(p, i.getItem()))) {
            double d = ie.distanceToSqr(p);
            if (d > r * r || d < 4.0 || d >= bestD) continue;
            best = ie;
            bestD = d;
        }
        if (best == null) return;
        var reporter = new ProblemReporter.ScopedCollector(() -> "polly", com.iiampanda.icbreed.IcBreed.LOGGER);
        Entity e;
        try {
            e = EntityType.create(TagValueInput.create(reporter, level.registryAccess(), tag), level,
                    new EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElse(null);
        } finally {
            reporter.close();
        }
        if (!(e instanceof Parrot parrot)) return;
        parrot.setPos(p.getX(), p.getY() + p.getBbHeight() * 0.85, p.getZ());
        parrot.setNoAi(true);
        parrot.setNoGravity(true);
        parrot.setPermanentlyInvulnerable(true);
        StayPut.sit(parrot, false);
        level.addWithUUID(parrot);
        if (useLeft) ((ServerPlayerAccessor) p).icbreed$setLeft(new CompoundTag());
        else ((ServerPlayerAccessor) p).icbreed$setRight(new CompoundTag());
        Mission m = new Mission();
        m.parrot = parrot.getUUID();
        m.item = best.getUUID();
        MISSIONS.put(p.getUUID(), m);
    }

    private static void mission(ServerPlayer p, Mission m) {
        ServerLevel level = p.level();
        Entity pe = level.getEntity(m.parrot);
        if (!(pe instanceof Parrot parrot) || !parrot.isAlive() || !p.isAlive()) {
            MISSIONS.remove(p.getUUID());
            finish(level.getServer(), p, m);
            return;
        }
        m.ticks++;
        Vec3 target;
        if (!m.returning) {
            Entity it = level.getEntity(m.item);
            if (it instanceof ItemEntity ie && ie.isAlive() && m.ticks < 400) {
                target = ie.position().add(0, 0.3, 0);
            } else {
                m.returning = true;
                target = p.position();
            }
        } else {
            target = p.position();
        }
        if (m.returning) target = p.position().add(0, p.getBbHeight() * 0.85, 0);
        Vec3 d = target.subtract(parrot.position());
        double dist = d.length();
        if (!m.returning && dist < 0.9 && level.getEntity(m.item) instanceof ItemEntity ie) {
            m.carry = ie.getItem().copy();
            ie.discard();
            m.returning = true;
            level.playSound(null, parrot.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.5F, 1.4F);
            return;
        }
        if (m.returning && (dist < 1.3 || m.ticks > 700)) {
            MISSIONS.remove(p.getUUID());
            finish(level.getServer(), p, m);
            return;
        }
        if (dist < 1.0E-4) return;
        Vec3 step = d.scale(Math.min(0.5, dist) / dist);
        float yaw = (float) (Mth.atan2(d.z, d.x) * (180.0 / Math.PI)) - 90.0F;
        parrot.setPos(parrot.position().add(step));
        parrot.setYRot(yaw);
        parrot.setYHeadRot(yaw);
        parrot.setDeltaMovement(step);
        parrot.setOnGround(false);
    }

    private static void finish(net.minecraft.server.MinecraftServer server, ServerPlayer p, Mission m) {
        ItemStack c = m.carry;
        Entity pe = null;
        for (ServerLevel l : server.getAllLevels()) {
            pe = l.getEntity(m.parrot);
            if (pe != null) break;
        }
        if (!c.isEmpty()) {
            if (p != null && p.isAlive()) {
                p.getInventory().add(c);
                if (!c.isEmpty()) p.drop(c, false, net.minecraft.util.Prediction.SERVER_ONLY);
                p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.2F);
            } else if (pe != null) {
                pe.level().addFreshEntity(new ItemEntity(pe.level(), pe.getX(), pe.getY(), pe.getZ(), c));
            }
        }
        if (pe instanceof Parrot parrot) {
            parrot.setNoAi(false);
            parrot.setNoGravity(false);
            parrot.setPermanentlyInvulnerable(false);
            if (p != null && p.isAlive() && p.level() == parrot.level()) {
                if (!parrot.setEntityOnShoulder(p)) StayPut.sit(parrot, false);
            }
        }
        if (p != null) NEXT.put(p.getUUID(), p.level().getGameTime() + 40);
    }
}
