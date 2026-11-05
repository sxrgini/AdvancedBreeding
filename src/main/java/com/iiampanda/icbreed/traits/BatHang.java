package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class BatHang {
    public static final int RADIUS = 20;
    private static final int GIVE_UP_TICKS = 1200;
    private static final int RETRY_TICKS = 200;

    private static final class State {
        BlockPos target;
        int seeking;
        long nextSearch;
        double hoverY = Double.NaN;
    }

    private static final Map<Bat, State> STATES = new WeakHashMap<>();

    private BatHang() {
    }

    public static boolean wantsHang(Bat bat) {
        return Boolean.TRUE.equals(com.iiampanda.icbreed.platform.Att.get(bat, Genetics.SITTING)) && Genetics.isTamed(bat);
    }

    public static void begin(Bat bat) {
        STATES.remove(bat);
    }

    public static void end(Bat bat) {
        STATES.remove(bat);
        if (bat.isResting()) bat.setResting(false);
    }

    public static BlockPos target(Bat bat) {
        State st = STATES.get(bat);
        return st == null ? null : st.target;
    }

    public static boolean control(ServerLevel level, Bat bat) {
        if (bat.isVehicle() || bat.isPassenger()) return false;
        if (wantsHang(bat)) {
            hang(level, bat);
            return true;
        }
        if (!bat.isBaby()) {
            Bat mate = BatBreeding.mate(level, bat);
            if (mate != null) {
                if (bat.isResting()) bat.setResting(false);
                steer(bat, mate.getX(), mate.getY() + 0.1, mate.getZ());
                return true;
            }
        }
        return false;
    }

    private static boolean ceiling(ServerLevel level, BlockPos cell) {
        BlockPos above = cell.above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }

    private static boolean free(ServerLevel level, BlockPos cell) {
        BlockState s = level.getBlockState(cell);
        return s.getCollisionShape(level, cell).isEmpty() && s.getFluidState().isEmpty();
    }

    private static boolean valid(ServerLevel level, BlockPos cell) {
        return level.hasChunkAt(cell) && free(level, cell) && ceiling(level, cell);
    }

    private static void hang(ServerLevel level, Bat bat) {
        State st = STATES.computeIfAbsent(bat, b -> new State());
        BlockPos here = bat.blockPosition();
        if (bat.isResting()) {
            if (ceiling(level, here)) {
                if (bat.getRandom().nextInt(200) == 0) bat.yHeadRot = bat.getRandom().nextInt(360);
                return;
            }
            bat.setResting(false);
            st.target = null;
            st.nextSearch = 0;
        }
        long now = level.getGameTime();
        if (st.target != null && !valid(level, st.target)) st.target = null;
        if (st.target == null && now >= st.nextSearch) {
            st.target = find(level, bat);
            st.seeking = 0;
            st.nextSearch = now + (st.target == null ? RETRY_TICKS : 20);
        }
        if (st.target == null) {
            if (Double.isNaN(st.hoverY)) st.hoverY = bat.getY();
            steer(bat, bat.getX(), st.hoverY + 0.1, bat.getZ());
            return;
        }
        st.hoverY = Double.NaN;
        Vec3 center = Vec3.atCenterOf(st.target);
        if (bat.position().add(0, bat.getBbHeight() * 0.5, 0).distanceToSqr(center) < 1.0) {
            bat.setPos(center.x, st.target.getY() + 0.1, center.z);
            bat.setDeltaMovement(Vec3.ZERO);
            bat.setResting(true);
            return;
        }
        if (++st.seeking > GIVE_UP_TICKS) {
            st.target = null;
            st.nextSearch = now + RETRY_TICKS * 3L;
            return;
        }
        steer(bat, center.x, st.target.getY() + 0.1, center.z);
    }

    public static BlockPos find(ServerLevel level, Bat bat) {
        BlockPos origin = bat.blockPosition();
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
                for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                    if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) continue;
                    int y = origin.getY() + dy;
                    if (y <= minY || y >= maxY) continue;
                    p.set(origin.getX() + dx, y, origin.getZ() + dz);
                    if (!level.hasChunkAt(p) || !free(level, p) || !ceiling(level, p)) continue;
                    found.add(p.immutable());
                }
            }
        }
        if (found.isEmpty()) return null;
        found.sort((a, b) -> Double.compare(a.distSqr(origin), b.distSqr(origin)));
        Vec3 eye = bat.getEyePosition();
        int checked = 0;
        for (BlockPos c : found) {
            if (checked++ >= 1500) break;
            var hit = level.clip(new ClipContext(eye, Vec3.atCenterOf(c), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bat));
            if (hit.getType() == HitResult.Type.MISS) return c;
        }
        return null;
    }

    public static void steer(Bat bat, double tx, double ty, double tz) {
        double dx = tx - bat.getX();
        double dy = ty - bat.getY();
        double dz = tz - bat.getZ();
        Vec3 m = bat.getDeltaMovement();
        Vec3 nm = m.add((Math.signum(dx) * 0.5 - m.x) * 0.1F, (Math.signum(dy) * 0.7F - m.y) * 0.1F, (Math.signum(dz) * 0.5 - m.z) * 0.1F);
        bat.setDeltaMovement(nm);
        float yaw = (float) (Mth.atan2(nm.z, nm.x) * 180.0F / (float) Math.PI) - 90.0F;
        float diff = Mth.wrapDegrees(yaw - bat.getYRot());
        bat.zza = 0.5F;
        bat.setYRot(bat.getYRot() + diff);
    }
}
