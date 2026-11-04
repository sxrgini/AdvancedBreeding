package com.iiampanda.icbreed.taming;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.block.ModBlocks;
import com.iiampanda.icbreed.genetics.Ages;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Taming;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction.Axis;

public final class TamingLogic {
    public static final ThreadLocal<Boolean> ALLOW = ThreadLocal.withInitial(() -> false);

    private static final int PARENT_RANGE = com.iiampanda.icbreed.IcBreedConfig.get().parentRange;

    private TamingLogic() {
    }

    public static boolean blocksVanillaTaming(Object self) {
        return self instanceof Mob a && Genetics.supports(a) && !ALLOW.get() && !a.level().isClientSide() && Genetics.isWild(a);
    }

    public static void onAdultDied(Mob dead) {
        if (Ages.isBaby(dead) || !(dead.level() instanceof ServerLevel level)) return;
        if (!(dead.getLastHurtByMob() instanceof net.minecraft.world.entity.player.Player)) return;
        AABB box = dead.getBoundingBox().inflate(PARENT_RANGE);
        List<? extends Mob> babies = level.getEntitiesOfClass(babyClass(dead), box,
                a -> a != dead && Ages.isBaby(a) && a.isAlive());
        for (Mob baby : babies) {
            Taming t = com.iiampanda.icbreed.platform.Att.get(baby, Genetics.TAMING);
            if (t == null || t.tamed() || t.orphan()) continue;
            com.iiampanda.icbreed.platform.Att.set(baby, Genetics.TAMING, t.withOrphan());
        }
    }

    public static void onFed(Mob animal, UUID player) {
        Taming t = com.iiampanda.icbreed.platform.Att.get(animal, Genetics.TAMING);
        if (t == null || t.tamed() || !t.orphan() || !Ages.isBaby(animal)) return;
        var fx = Genetics.effects(animal);
        double stretch = fx == null ? 1.0 : fx.hungerStretch();
        long until = animal.level().getGameTime() + Math.round(IcBreedConfig.get().tamingFoodSeconds * 20L * stretch);
        com.iiampanda.icbreed.platform.Att.set(animal, Genetics.TAMING, t.withFed(until, player));
    }

    private static final double GROWN_YOUNG_CHANCE = com.iiampanda.icbreed.IcBreedConfig.get().grownYoungChance;

    public static boolean tick(ServerLevel level, Mob animal) {
        Taming t = com.iiampanda.icbreed.platform.Att.get(animal, Genetics.TAMING);
        if (t == null || t.tamed()) return true;
        if (!Ages.isBaby(animal) && !t.young()) {
            ensureYoung(level, animal, t.orphan() || t.started() ? GROWN_YOUNG_CHANCE : IcBreedConfig.get().youngSpawnChance);
            t = com.iiampanda.icbreed.platform.Att.get(animal, Genetics.TAMING);
        }
        if (Ages.isBaby(animal) && !t.orphan()) {
            holdAsBaby(animal);
            return false;
        }

        long now = level.getGameTime();
        boolean leashed = animal.isLeashed();
        boolean pen = Ages.isBaby(animal) ? isEnclosed(level, animal.blockPosition()) : t.pen();
        boolean bed = Ages.isBaby(animal) ? hasBed(level, animal.blockPosition(), IcBreedConfig.get().bedRadius) : t.bed();

        if (!Ages.isBaby(animal)) {
            if (t.started() && t.pen() && t.bed()) complete(level, animal, t);
            return true;
        }

        Taming next = t;
        if (leashed && !t.led()) next = next.withLed();

        if (!next.started() && leashed && bed && next.fed(now)) next = next.withStart(Ages.getAge(animal));
        if (pen != next.pen() || bed != next.bed()) next = next.withChecks(pen, bed);
        if (!next.equals(t)) com.iiampanda.icbreed.platform.Att.set(animal, Genetics.TAMING, next);

        boolean progress = next.started() && pen && bed && next.fed(now);
        if (!next.started()) {
            holdAsBaby(animal);
            return false;
        }
        if (!progress) {
            Ages.setAge(animal, Math.min(Ages.getAge(animal) - 20, -40));
            return false;
        }
        return true;
    }

    public static Class<? extends Mob> babyClass(Mob adult) {
        return adult instanceof net.minecraft.world.entity.animal.frog.Frog ? net.minecraft.world.entity.animal.frog.Tadpole.class : adult.getClass();
    }

    private static BlockPos findWater(ServerLevel level, BlockPos origin, int radius) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    p.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.hasChunkAt(p) || !level.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)) continue;
                    if (!level.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)) continue;
                    double d = p.distSqr(origin);
                    if (d < bestDist) {
                        bestDist = d;
                        best = p.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static void ensureYoung(ServerLevel level, Mob adult, double chance) {
        Taming t = com.iiampanda.icbreed.platform.Att.get(adult, Genetics.TAMING);
        if (t == null || t.young()) return;
        AABB box = adult.getBoundingBox().inflate(PARENT_RANGE);
        boolean has = !level.getEntitiesOfClass(babyClass(adult), box, a -> a != adult && Ages.isBaby(a) && a.isAlive()).isEmpty();
        if (!has) {
            int crowd = level.getEntitiesOfClass(adult.getClass(), adult.getBoundingBox().inflate(16.0), a -> a != adult && a.isAlive()).size();
            if (crowd >= 10 || adult.getRandom().nextDouble() >= chance) {
                com.iiampanda.icbreed.platform.Att.set(adult, Genetics.TAMING, t.withYoung());
                return;
            }
            net.minecraft.world.entity.Entity made = adult instanceof net.minecraft.world.entity.animal.frog.Frog
                    ? net.minecraft.world.entity.EntityTypes.TADPOLE.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING)
                    : adult.getType().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
            if (made instanceof Mob baby && (baby instanceof net.minecraft.world.entity.AgeableMob || baby instanceof net.minecraft.world.entity.animal.frog.Tadpole)) {
                double x = adult.getX() + (adult.getRandom().nextDouble() - 0.5) * 2;
                double y = adult.getY();
                double z = adult.getZ() + (adult.getRandom().nextDouble() - 0.5) * 2;
                if (baby instanceof net.minecraft.world.entity.animal.frog.Tadpole) {
                    BlockPos water = findWater(level, adult.blockPosition(), 12);
                    if (water == null) return;
                    x = water.getX() + 0.5;
                    y = water.getY();
                    z = water.getZ() + 0.5;
                } else if (baby instanceof net.minecraft.world.entity.AgeableMob am) {
                    am.setBaby(true);
                }
                baby.setPersistenceRequired();
                adult.setPersistenceRequired();
                baby.snapTo(x, y, z, adult.getRandom().nextFloat() * 360.0F, 0.0F);
                level.addFreshEntityWithPassengers(baby);
            }
        }
        com.iiampanda.icbreed.platform.Att.set(adult, Genetics.TAMING, t.withYoung());
    }

    private static void holdAsBaby(Mob animal) {
        if (Ages.getAge(animal) > net.minecraft.world.entity.AgeableMob.BABY_START_AGE) {
            Ages.setAge(animal, net.minecraft.world.entity.AgeableMob.BABY_START_AGE);
        }
    }

    public static void forceTame(ServerLevel level, Mob animal, ServerPlayer owner) {
        Taming t = com.iiampanda.icbreed.platform.Att.get(animal, Genetics.TAMING);
        if (t == null) t = Taming.wild();
        animal.setPersistenceRequired();
        complete(level, animal, t.withOwner(owner.getUUID()));
    }

    private static void complete(ServerLevel level, Mob animal, Taming t) {
        com.iiampanda.icbreed.platform.Att.set(animal, Genetics.TAMING, t.asTamed());
        com.iiampanda.icbreed.platform.Att.set(animal, Genetics.LAST_FED, animal.level().getGameTime());
        ServerPlayer owner = t.owner().map(id -> level.getServer().getPlayerList().getPlayer(id)).orElse(null);
        ALLOW.set(true);
        try {
            if (animal instanceof TamableAnimal tamable) {
                if (owner != null) tamable.tame(owner);
                else tamable.setTame(true, true);
            } else if (animal instanceof AbstractHorse horse) {
                horse.setTamed(true);
                if (owner != null) horse.setOwner(owner);
            }
        } finally {
            ALLOW.set(false);
        }
        Component msg = Component.translatable("advbreeding.taming.done", Genetics.displayName(animal))
                .withStyle(net.minecraft.ChatFormatting.GREEN);
        notifyNearby(level, animal, msg);
    }

    private static void notifyNearby(ServerLevel level, Mob animal, Component msg) {
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(animal) < 32.0 * 32.0) p.sendSystemMessage(msg);
        }
    }

    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public static boolean isEnclosed(ServerLevel level, BlockPos start) {
        int max = IcBreedConfig.get().penMaxTiles;
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (!hasFloor(level, pos)) return false;
            for (Direction d : SIDES) {
                BlockPos n = pos.relative(d);
                if (seen.contains(n) || blocks(level, n)) continue;
                if (seen.size() >= max) return false;
                seen.add(n);
                queue.add(n);
            }
        }
        return true;
    }

    private static boolean hasFloor(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!level.hasChunkAt(below)) return false;
        if (level.getFluidState(below).is(net.minecraft.tags.FluidTags.WATER)) return true;
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty() || blocks(level, below);
    }

    private static boolean blocks(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return true;
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        return !shape.isEmpty() && shape.max(Axis.Y) > 0.5;
    }

    public static boolean hasBed(ServerLevel level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    if (dx * dx + dz * dz + dy * dy > r2) continue;
                    p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.hasChunkAt(p) || !level.getBlockState(p).is(ModBlocks.HAY_CARPET)) continue;
                    if (isBedCorner(level, p)) return true;
                }
            }
        }
        return false;
    }

    private static boolean isBedCorner(ServerLevel level, BlockPos pos) {
        for (int ox = -1; ox <= 0; ox++) {
            for (int oz = -1; oz <= 0; oz++) {
                if (square(level, pos.getX() + ox, pos.getY(), pos.getZ() + oz, ModBlocks.HAY_CARPET)) return true;
            }
        }
        return false;
    }

    public static boolean square(net.minecraft.world.level.LevelReader level, int x, int y, int z, net.minecraft.world.level.block.Block block) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                p.set(x + dx, y, z + dz);
                if (!level.hasChunkAt(p) || !level.getBlockState(p).is(block)) return false;
            }
        }
        return true;
    }
}
