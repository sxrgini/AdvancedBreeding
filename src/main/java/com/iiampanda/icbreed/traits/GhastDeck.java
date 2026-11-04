package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.mixin.BlockDisplayAccessor;
import com.iiampanda.icbreed.mixin.DisplayAccessor;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.iiampanda.icbreed.platform.AttachmentRegistry;
import com.iiampanda.icbreed.platform.AttachmentSyncPredicate;
import net.neoforged.neoforge.attachment.AttachmentType;
import com.iiampanda.icbreed.platform.ServerLivingEntityEvents;
import com.iiampanda.icbreed.platform.ServerTickEvents;
import com.iiampanda.icbreed.platform.UseBlockCallback;
import com.iiampanda.icbreed.platform.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GhastDeck {
    public record Entry(long off, BlockState state, Optional<CompoundTag> tag) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("off").forGetter(Entry::off),
                BlockState.CODEC.fieldOf("state").forGetter(Entry::state),
                CompoundTag.CODEC.optionalFieldOf("tag").forGetter(Entry::tag)).apply(i, Entry::new));
    }

    public record Deck(long anchor, List<Long> offsets, List<Entry> flight) {
        public static final Codec<Deck> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("anchor").forGetter(Deck::anchor),
                Codec.LONG.listOf().fieldOf("offsets").forGetter(Deck::offsets),
                Entry.CODEC.listOf().fieldOf("flight").forGetter(Deck::flight)).apply(i, Deck::new));

        public boolean flying() {
            return !flight.isEmpty();
        }
    }

    public record Seats(List<UUID> riders, List<Long> offsets) {
        public static final Codec<Seats> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.listOf().fieldOf("riders").forGetter(Seats::riders),
                Codec.LONG.listOf().fieldOf("offsets").forGetter(Seats::offsets)).apply(i, Seats::new));
    }

    public static final AttachmentType<Deck> DECK = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "ghast_deck"),
            builder -> builder.persistent(Deck.CODEC));

    public static final AttachmentType<Seats> SEATS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "ghast_seats"),
            builder -> builder.persistent(Seats.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(Seats.CODEC), AttachmentSyncPredicate.all()));

    private static final String TAG = "advbreeding_deck";
    private static final int RADIUS = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckRadius;
    private static final int MIN_Y = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckMinY;
    private static final int MAX_Y = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckMaxY;
    private static final int MAX_BLOCKS = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckMaxBlocks;
    private static final int MAX_SEATS = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckMaxSeats;
    private static final int SETTLE_TICKS = com.iiampanda.icbreed.IcBreedConfig.get().ghastDeckSettleTicks;
    private static final int FLAGS = 2 | 16;

    private static final class Motion {
        Vec3 ref;
        int still;
    }

    private static final Map<UUID, Motion> MOTION = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> DOCK = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> FIX = new ConcurrentHashMap<>();

    private record Pending(ServerLevel level, BlockPos pos, UUID ghast) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private GhastDeck() {
    }

    public static boolean buildable(HappyGhast g) {
        if (!Genetics.isTamed(g)) return false;
        Effects fx = Genetics.effects(g);
        return fx != null && fx.buildable > 0;
    }

    public static boolean isDeckPart(Entity e) {
        return e instanceof Display.BlockDisplay && e.entityTags().contains(TAG);
    }

    public static boolean hasBlocks(HappyGhast g) {
        Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
        if (d == null) return false;
        if (d.flying()) return true;
        if (!(g.level() instanceof ServerLevel level)) return !d.offsets().isEmpty();
        BlockPos a = BlockPos.of(d.anchor());
        for (long o : d.offsets()) {
            if (!level.getBlockState(a.offset(BlockPos.of(o))).isAir()) return true;
        }
        return false;
    }

    public static LivingEntity seated(HappyGhast g) {
        Seats s = com.iiampanda.icbreed.platform.Att.get(g, SEATS);
        if (s == null || s.riders().isEmpty()) return null;
        for (Entity p : g.getPassengers()) {
            if (p instanceof Player player && s.riders().contains(player.getUUID())) return player;
        }
        return null;
    }

    public static Vec3 attach(HappyGhast g, Entity passenger) {
        if (passenger instanceof Display.BlockDisplay) return Vec3.ZERO;
        if (passenger instanceof Player) {
            Seats s = com.iiampanda.icbreed.platform.Att.get(g, SEATS);
            if (s == null) return null;
            int i = s.riders().indexOf(passenger.getUUID());
            if (i < 0) return null;
            BlockPos o = BlockPos.of(s.offsets().get(i));
            return new Vec3(o.getX() + 0.5, o.getY() + 0.5, o.getZ() + 0.5);
        }
        return null;
    }

    private static BlockPos companion(BlockState s, BlockPos p) {
        if (s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock) {
            if (!s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) return null;
            return s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
                    == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER ? p.above() : p.below();
        }
        if (s.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            Direction f = s.getValue(net.minecraft.world.level.block.BedBlock.FACING);
            return s.getValue(net.minecraft.world.level.block.BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.FOOT
                    ? p.relative(f) : p.relative(f.getOpposite());
        }
        return null;
    }

    private static boolean inBounds(BlockPos off) {
        return Math.abs(off.getX()) <= RADIUS && Math.abs(off.getZ()) <= RADIUS
                && off.getY() >= MIN_Y && off.getY() < MAX_Y;
    }

    private static BlockPos snapped(HappyGhast g) {
        return new BlockPos((int) Math.round(g.getX()), (int) Math.round(g.getY()), (int) Math.round(g.getZ()));
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof HappyGhast g) || !buildable(g)) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof BlockItem)) return InteractionResult.PASS;
            Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
            if (d != null && d.flying()) return InteractionResult.PASS;
            Vec3 at = hit != null ? hit.getLocation() : g.position();
            if (at.y < g.getBoundingBox().maxY - 0.75) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ServerLevel sl = (ServerLevel) level;
            BlockPos anchor = d == null ? snapped(g) : BlockPos.of(d.anchor());
            BlockPos target = new BlockPos(Mth.floor(at.x), anchor.getY() + MIN_Y, Mth.floor(at.z));
            BlockPos off = target.subtract(anchor);
            if (!inBounds(off) || !sl.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;
            if (d != null && d.offsets().size() >= MAX_BLOCKS) return InteractionResult.PASS;
            if (d == null) {
                g.snapTo(anchor.getX(), anchor.getY(), anchor.getZ());
                g.setDeltaMovement(Vec3.ZERO);
                DOCK.put(g.getUUID(), g.position());
                FIX.put(g.getUUID(), Vec3.ZERO);
            }
            BlockHitResult bhr = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
            InteractionResult r = held.useOn(new UseOnContext(player, hand, bhr));
            if (r.consumesAction()) {
                if (d == null) d = new Deck(anchor.asLong(), new ArrayList<>(), List.of());
                addOffset(g, d, off);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.isClientSide() || player.isSpectator() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            ServerLevel sl = (ServerLevel) level;
            BlockPos clicked = hit.getBlockPos();
            List<HappyGhast> near = sl.getEntitiesOfClass(HappyGhast.class, new AABB(clicked).inflate(RADIUS + 6),
                    g -> com.iiampanda.icbreed.platform.Att.get(g, DECK) != null && !com.iiampanda.icbreed.platform.Att.get(g, DECK).flying());
            for (HappyGhast g : near) {
                Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
                BlockPos off = clicked.subtract(BlockPos.of(d.anchor()));
                if (!d.offsets().contains(off.asLong())) continue;
                if (player.getItemInHand(hand).isEmpty() && !player.isShiftKeyDown()
                        && sl.getBlockState(clicked).getBlock() instanceof StairBlock) {
                    if (mount(g, player, off)) return InteractionResult.SUCCESS;
                    return InteractionResult.PASS;
                }
                BlockPos target = sl.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(hit.getDirection());
                synchronized (PENDING) {
                    PENDING.add(new Pending(sl, target, g.getUUID()));
                }
                break;
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<Pending> list;
            synchronized (PENDING) {
                if (PENDING.isEmpty()) return;
                list = new ArrayList<>(PENDING);
                PENDING.clear();
            }
            for (Pending p : list) {
                Entity e = p.level().getEntity(p.ghast());
                if (!(e instanceof HappyGhast g)) continue;
                Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
                if (d == null || d.flying() || p.level().getBlockState(p.pos()).isAir()) continue;
                if (d.offsets().size() >= MAX_BLOCKS) continue;
                addOffset(g, d, p.pos().subtract(BlockPos.of(d.anchor())));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof HappyGhast g && g.level() instanceof ServerLevel level) {
                Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
                if (d != null && d.flying()) land(g, level, d, true);
            }
        });
    }

    private static boolean mount(HappyGhast g, Player player, BlockPos off) {
        Seats s = com.iiampanda.icbreed.platform.Att.get(g, SEATS);
        List<UUID> riders = s == null ? new ArrayList<>() : new ArrayList<>(s.riders());
        List<Long> offs = s == null ? new ArrayList<>() : new ArrayList<>(s.offsets());
        if (offs.contains(off.asLong()) || riders.size() >= MAX_SEATS || riders.contains(player.getUUID())) return false;
        riders.add(player.getUUID());
        offs.add(off.asLong());
        com.iiampanda.icbreed.platform.Att.set(g, SEATS, new Seats(riders, offs));
        return player.startRiding(g, true, false);
    }

    private static void cleanSeats(HappyGhast g) {
        Seats s = com.iiampanda.icbreed.platform.Att.get(g, SEATS);
        if (s == null) return;
        List<UUID> riders = new ArrayList<>();
        List<Long> offs = new ArrayList<>();
        for (int i = 0; i < s.riders().size(); i++) {
            UUID id = s.riders().get(i);
            boolean on = false;
            for (Entity p : g.getPassengers()) {
                if (p.getUUID().equals(id)) {
                    on = true;
                    break;
                }
            }
            if (on) {
                riders.add(id);
                offs.add(s.offsets().get(i));
            }
        }
        if (riders.size() == s.riders().size()) return;
        if (riders.isEmpty()) com.iiampanda.icbreed.platform.Att.remove(g, SEATS);
        else com.iiampanda.icbreed.platform.Att.set(g, SEATS, new Seats(riders, offs));
    }

    private static void addOffset(HappyGhast g, Deck d, BlockPos off) {
        if (!inBounds(off)) return;
        List<Long> offs = new ArrayList<>(d.offsets());
        if (!offs.contains(off.asLong())) offs.add(off.asLong());
        com.iiampanda.icbreed.platform.Att.set(g, DECK, new Deck(d.anchor(), offs, d.flight()));
    }

    public static void tick(HappyGhast g) {
        Deck d = com.iiampanda.icbreed.platform.Att.get(g, DECK);
        if (d == null || !(g.level() instanceof ServerLevel level)) return;
        if (!buildable(g)) return;
        cleanSeats(g);
        if (d.flying()) {
            flightTick(g, level, d);
            return;
        }
        if (g.tickCount % 10 == 0) {
            d = prune(g, level, d);
            if (d == null) return;
        }
        BlockPos anchor = BlockPos.of(d.anchor());
        if (seated(g) == null) {
            ((com.iiampanda.icbreed.mixin.HappyGhastAccessor) g).icbreed$setStill(10);
            AABB zone = new AABB(anchor.getX() - RADIUS - 1, anchor.getY() - 1, anchor.getZ() - RADIUS - 1,
                    anchor.getX() + RADIUS + 2, anchor.getY() + MAX_Y + 1, anchor.getZ() + RADIUS + 2);
            for (Player p : level.players()) {
                if (!p.isSpectator() && !p.isPassenger() && zone.contains(p.position())) {
                    ((com.iiampanda.icbreed.mixin.HappyGhastAccessor) g).icbreed$setStill(80);
                    break;
                }
            }
        }
        Vec3 dock = DOCK.computeIfAbsent(g.getUUID(), k -> g.position());
        if (g.position().distanceToSqr(dock) > 0.0625) startFlight(g, level, d);
    }

    private static Deck prune(HappyGhast g, ServerLevel level, Deck d) {
        BlockPos a = BlockPos.of(d.anchor());
        List<Long> kept = new ArrayList<>();
        for (long o : d.offsets()) {
            BlockPos p = a.offset(BlockPos.of(o));
            if (!level.hasChunkAt(p)) return d;
            if (!level.getBlockState(p).isAir()) kept.add(o);
        }
        if (kept.size() == d.offsets().size()) return d;
        if (kept.isEmpty()) {
            com.iiampanda.icbreed.platform.Att.remove(g, DECK);
            return null;
        }
        Deck nd = new Deck(d.anchor(), kept, List.of());
        com.iiampanda.icbreed.platform.Att.set(g, DECK, nd);
        return nd;
    }

    private static void startFlight(HappyGhast g, ServerLevel level, Deck d) {
        BlockPos a = BlockPos.of(d.anchor());
        List<Entry> entries = new ArrayList<>();
        List<Long> kept = new ArrayList<>();
        List<BlockPos> world = new ArrayList<>();
        java.util.LinkedHashSet<Long> all = new java.util.LinkedHashSet<>();
        for (long o : d.offsets()) {
            all.add(o);
            BlockPos p = a.offset(BlockPos.of(o));
            if (!level.hasChunkAt(p)) return;
            BlockPos other = companion(level.getBlockState(p), p);
            if (other != null && level.hasChunkAt(other)) all.add(other.subtract(a).asLong());
        }
        for (long o : all) {
            BlockPos p = a.offset(BlockPos.of(o));
            if (!level.hasChunkAt(p)) return;
            BlockState s = level.getBlockState(p);
            if (s.isAir()) continue;
            BlockEntity be = level.getBlockEntity(p);
            entries.add(new Entry(o, s, be == null ? Optional.empty() : Optional.of(be.saveWithFullMetadata(level.registryAccess()))));
            kept.add(o);
            world.add(p);
        }
        if (entries.isEmpty()) {
            com.iiampanda.icbreed.platform.Att.remove(g, DECK);
            return;
        }
        for (BlockPos p : world) {
            level.removeBlockEntity(p);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), FLAGS);
        }
        Vec3 pos = g.position();
        if (!FIX.containsKey(g.getUUID())) {
            Vec3 o = Vec3.atLowerCornerOf(a).subtract(DOCK.getOrDefault(g.getUUID(), pos));
            FIX.put(g.getUUID(), new Vec3(Mth.clamp(o.x, -0.5, 0.5), Mth.clamp(o.y, -0.5, 0.5), Mth.clamp(o.z, -0.5, 0.5)));
        }
        for (Entry e : entries) {
            BlockPos o = BlockPos.of(e.off());
            Display.BlockDisplay bd = net.minecraft.world.entity.EntityTypes.BLOCK_DISPLAY.create(level, EntitySpawnReason.TRIGGERED);
            if (bd == null) continue;
            bd.setPos(pos);
            ((BlockDisplayAccessor) bd).icbreed$setBlockState(e.state());
            Vector3f t = new Vector3f((float) (a.getX() + o.getX() - pos.x), (float) (a.getY() + o.getY() - pos.y), (float) (a.getZ() + o.getZ() - pos.z));
            ((DisplayAccessor) bd).icbreed$setTransformation(new Transformation(t, new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf()));
            bd.addTag(TAG);
            level.addFreshEntity(bd);
            bd.startRiding(g, true, false);
        }
        com.iiampanda.icbreed.platform.Att.set(g, DECK, new Deck(d.anchor(), kept, entries));
        MOTION.remove(g.getUUID());
    }

    private static void flightTick(HappyGhast g, ServerLevel level, Deck d) {
        int shown = 0;
        for (Entity p : g.getPassengers()) {
            if (isDeckPart(p)) shown++;
        }
        if (shown != d.flight().size()) {
            land(g, level, d, true);
            return;
        }
        if (seated(g) == null) ((com.iiampanda.icbreed.mixin.HappyGhastAccessor) g).icbreed$setStill(10);
        Motion m = MOTION.computeIfAbsent(g.getUUID(), k -> new Motion());
        Vec3 now = g.position();
        if (m.ref != null && now.distanceToSqr(m.ref) < 0.16) {
            m.still += g.staysStill() && seated(g) == null ? 3 : 1;
        } else {
            m.ref = now;
            m.still = 0;
        }
        if (m.still >= SETTLE_TICKS) land(g, level, d, false);
    }

    private static void land(HappyGhast g, ServerLevel level, Deck d, boolean force) {
        Vec3 now = g.position();
        Vec3 fix = FIX.getOrDefault(g.getUUID(), Vec3.ZERO);
        BlockPos a = new BlockPos((int) Math.round(now.x + fix.x), (int) Math.round(now.y + fix.y), (int) Math.round(now.z + fix.z));
        if (!force) {
            for (Entry e : d.flight()) {
                if (!level.hasChunkAt(a.offset(BlockPos.of(e.off())))) return;
            }
        }
        for (Entity p : new ArrayList<>(g.getPassengers())) {
            if (isDeckPart(p)) p.discard();
        }
        if (seated(g) == null) g.setDeltaMovement(Vec3.ZERO);
        DOCK.put(g.getUUID(), g.position());
        List<Long> kept = new ArrayList<>();
        for (Entry e : d.flight()) {
            BlockPos np = a.offset(BlockPos.of(e.off()));
            BlockState current = level.getBlockState(np);
            if (!current.isAir() && !current.canBeReplaced()) {
                Block.popResource(level, np, new ItemStack(e.state().getBlock()));
                continue;
            }
            level.setBlock(np, e.state(), FLAGS);
            if (e.tag().isPresent()) {
                BlockEntity be = BlockEntity.loadStatic(np, e.state(), e.tag().get(), level.registryAccess());
                if (be != null) level.setBlockEntity(be);
            }
            kept.add(e.off());
        }
        MOTION.remove(g.getUUID());
        if (kept.isEmpty()) com.iiampanda.icbreed.platform.Att.remove(g, DECK);
        else com.iiampanda.icbreed.platform.Att.set(g, DECK, new Deck(a.asLong(), kept, List.of()));
    }
}
