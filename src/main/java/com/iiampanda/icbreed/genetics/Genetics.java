package com.iiampanda.icbreed.genetics;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.IcBreedConfig;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;

public final class Genetics {
    public static final AttachmentType<Genome> GENOME = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "genome"),
            builder -> builder.persistent(Genome.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(Genome.CODEC), AttachmentSyncPredicate.all()));

    public static final AttachmentType<Pregnancy> PREGNANCY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "pregnancy"),
            builder -> builder.persistent(Pregnancy.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(Pregnancy.CODEC), AttachmentSyncPredicate.all()));

    public static final AttachmentType<Taming> TAMING = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "taming"),
            builder -> builder.persistent(Taming.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(Taming.CODEC), AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> GROWTH = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "growth"),
            builder -> builder.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Traits> TRAITS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "traits"),
            builder -> builder.persistent(Traits.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(Traits.CODEC), AttachmentSyncPredicate.all()));

    public static final AttachmentType<java.util.List<net.minecraft.world.item.ItemStack>> BAGS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "bags"),
            builder -> builder.persistent(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.listOf())
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.listOf()),
                            AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> BAG_STYLE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "bag_style"),
            builder -> builder.persistent(com.mojang.serialization.Codec.INT)
                    .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Long> MILK_READY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "milk_ready"),
            builder -> builder.persistent(com.mojang.serialization.Codec.LONG)
                    .syncWith(ByteBufCodecs.VAR_LONG, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Boolean> CLING = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "cling"),
            builder -> builder.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly()));

    public static final AttachmentType<Long> NINE_LIVES_READY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "nine_lives_ready"),
            builder -> builder.persistent(com.mojang.serialization.Codec.LONG));

    public static final AttachmentType<Boolean> SITTING = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "sitting"),
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL)
                    .syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL, net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.all()));

    public static final AttachmentType<Boolean> CULL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "cull"),
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL));

    public static final AttachmentType<Boolean> FOLLOWING = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "following"),
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL));

    public static final AttachmentType<Integer> PERKS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "shoulder_perks"),
            builder -> builder.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()));

    public static final AttachmentType<String> FROG_VARIANT = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "frog_variant"),
            builder -> builder.persistent(com.mojang.serialization.Codec.STRING));

    public static final AttachmentType<Boolean> JUMP_ROLLED = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "jump_rolled"),
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL));

    public static final AttachmentType<Long> LAST_FED = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "last_fed"),
            builder -> builder.persistent(com.mojang.serialization.Codec.LONG));

    public static final AttachmentType<Boolean> HONEY_HIVE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "honey_hive"),
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL));

    public static final AttachmentType<Integer> HONEY_PROGRESS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "honey_progress"),
            builder -> builder.persistent(com.mojang.serialization.Codec.INT));

    public static final AttachmentType<Long> LOVE_UNTIL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "love_until"));

    public static final ThreadLocal<net.minecraft.world.entity.animal.frog.Tadpole> CONVERTING = new ThreadLocal<>();

    public static final int XP_FEED = 2;
    public static final int XP_BREED = 50;
    public static final int XP_BIRTH = 25;
    public static final int XP_KILL = 25;

    public static final int XP_PRODUCE = 20;

    private Genetics() {
    }

    public static Genome getOrCreate(Mob animal) {
        Genome g = animal.getAttached(GENOME);
        if (g == null) {
            g = Genome.wild(new java.util.Random(animal.getUUID().getMostSignificantBits() ^ Long.rotateLeft(animal.getUUID().getLeastSignificantBits(), 17)));
            animal.setAttached(GENOME, g);
            applyAttributes(animal, g, true);
        }
        return g;
    }

    public static void init() {
    }

    public static void onLoad(Mob animal) {
        net.minecraft.world.entity.animal.frog.Tadpole src = CONVERTING.get();
        if (src != null && animal instanceof net.minecraft.world.entity.animal.frog.Frog) {
            Genome sg = src.getAttached(GENOME);
            if (sg != null) animal.setAttached(GENOME, sg);
            Colors.applyFrog(src, (net.minecraft.world.entity.animal.frog.Frog) animal);
            Taming st = src.getAttached(TAMING);
            if (st != null) animal.setAttached(TAMING, st.withYoung());
            Traits str = src.getAttached(TRAITS);
            if (str != null) animal.setAttached(TRAITS, str);
        }
        Genome g = animal.getAttached(GENOME);
        boolean fresh = false;
        if (g == null) {
            g = Genome.wild(new java.util.Random(animal.getUUID().getMostSignificantBits() ^ Long.rotateLeft(animal.getUUID().getLeastSignificantBits(), 17)));
            animal.setAttached(GENOME, g);
            fresh = true;
        }
        if (isTamedSpecies(animal)) {
            Taming cur = animal.getAttached(TAMING);
            if (cur == null || !cur.tamed()) animal.setAttached(TAMING, Taming.ownedRecord());
        }
        if (animal.getAttached(TAMING) == null) {
            boolean owned = alreadyTamed(animal) || (!fresh && (g.generation > 0 || g.hasParents() || animal.hasCustomName()));
            animal.setAttached(TAMING, owned ? Taming.ownedRecord() : Taming.wild());
        }
        if (animal instanceof net.minecraft.world.entity.monster.hoglin.Hoglin hog && (Ages.isBaby(hog) || isTamed(hog))) {
            hog.setImmuneToZombification(true);
        }
        com.iiampanda.icbreed.traits.MountJump.roll(animal);
        applyAttributes(animal, g, fresh || animal.tickCount < 2);
    }

    public static boolean isTamedSpecies(Mob animal) {
        return animal instanceof net.minecraft.world.entity.animal.camel.Camel
                && !(animal instanceof net.minecraft.world.entity.animal.camel.CamelHusk);
    }

    private static boolean alreadyTamed(Mob animal) {
        return (animal instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame())
                || (animal instanceof net.minecraft.world.entity.animal.equine.AbstractHorse h && h.isTamed());
    }

    public static boolean isWild(Mob animal) {
        Taming t = animal.getAttached(TAMING);
        return t != null && !t.tamed();
    }

    public static void updateGrowth(Mob animal) {
        Integer cur = animal.getAttached(GROWTH);
        Integer want = null;
        Taming t = animal.getAttached(TAMING);
        if (t != null && !t.tamed()) {
            want = Ages.isBaby(animal) ? t.progress(Ages.getAge(animal)) : 0;
        } else if (Ages.isBaby(animal)) {
            int age = Ages.getAge(animal);
            want = (int) Math.round(100.0 * clamp((AgeableMob.BABY_START_AGE * -1.0 + age) / (-AgeableMob.BABY_START_AGE), 0, 1));
        }
        if (want == null) {
            if (cur != null) animal.removeAttached(GROWTH);
        } else if (!want.equals(cur)) {
            animal.setAttached(GROWTH, want);
        }
    }

    public static boolean isTamed(Mob animal) {
        Taming t = animal.getAttached(TAMING);
        return t != null && t.tamed();
    }

    public static Effects effects(Mob animal) {
        Genome g = animal.getAttached(GENOME);
        if (g == null) return null;
        Traits t = animal.getAttached(TRAITS);
        return t == null ? g.effects() : t.combined(g, com.iiampanda.icbreed.traits.TraitTrees.forMob(animal));
    }

    public static int availablePoints(Genome g, Traits t, com.iiampanda.icbreed.traits.TraitTrees.Tree tree) {
        return Math.max(0, g.level() - (t == null ? 0 : t.spent(tree)));
    }

    public static boolean tryUpgrade(Mob animal, String traitId) {
        Genome g = animal.getAttached(GENOME);
        if (g == null || isWild(animal)) return false;
        var tree = com.iiampanda.icbreed.traits.TraitTrees.forMob(animal);
        var def = tree.get(traitId);
        if (def == null) return false;
        Traits t = animal.getAttached(TRAITS);
        if (t == null) t = Traits.NONE;
        int rank = t.rank(traitId);
        if (rank >= def.maxRank() || availablePoints(g, t, tree) < def.cost()) return false;
        if (def.missing(t::rank) != null) return false;
        animal.setAttached(TRAITS, t.with(traitId, rank + 1));
        applyAttributes(animal, g, false);
        return true;
    }

    public static void addXp(Mob animal, int amount) {
        Genome g = animal.getAttached(GENOME);
        if (g == null || g.level() >= Genome.MAX_LEVEL) return;
        Genome next = g.withXp(g.xp + amount);
        animal.setAttached(GENOME, next);
        if (next.level() != g.level()) applyAttributes(animal, next, false);
    }

    public static String displayName(Mob animal) {
        return (animal.hasCustomName() ? animal.getCustomName() : animal.getType().getDescription()).getString();
    }

    public static void inherit(Mob parentA, Mob parentB, Mob child) {
        Genome a = getOrCreate(parentA);
        Genome b = getOrCreate(parentB);
        boolean aMother = a.sex == Sex.FEMALE || (b.sex != Sex.FEMALE && ThreadLocalRandom.current().nextBoolean());
        Mob mom = aMother ? parentA : parentB;
        Mob dad = aMother ? parentB : parentA;
        child.setAttached(TAMING, Taming.ownedRecord());
        child.setAttached(GENOME, Genome.breed(aMother ? a : b, aMother ? b : a, ThreadLocalRandom.current(), false,
                displayName(mom), displayName(dad)));
        addXp(parentA, XP_BIRTH);
        addXp(parentB, XP_BIRTH);
    }

    public static boolean supports(net.minecraft.world.entity.Entity e) {
        return Ages.supports(e);
    }

    public static boolean canBreed(Mob a, Mob b) {
        if (isPregnant(a) || isPregnant(b) || isWild(a) || isWild(b)) return false;
        return getOrCreate(a).sex != getOrCreate(b).sex;
    }

    public static boolean isPregnant(Mob animal) {
        return animal.getAttached(PREGNANCY) != null;
    }

    public static void conceive(Mob mother, Mob father) {
        Genome fatherGenome = getOrCreate(father);
        mother.setAttached(PREGNANCY, new Pregnancy(fatherGenome, displayName(father), Optional.of(father.getUUID()),
                mother.level().getGameTime() + gestationTicks(mother), false));
        addXp(mother, XP_BREED);
        addXp(father, XP_BREED);
    }

    private static long gestationTicks(Mob mother) {
        Effects fx = effects(mother);
        double speed = 1.0 + (fx == null ? 0 : clamp(fx.gestationPct, -0.5, 3.0));
        return Math.max(20L, Math.round(IcBreedConfig.get().gestationSeconds * 20L / speed));
    }

    public static void onPregnantHit(Mob mother) {
        Pregnancy p = mother.getAttached(PREGNANCY);
        if (p == null || p.worse()) return;
        if (ThreadLocalRandom.current().nextDouble() < IcBreedConfig.get().worseChanceOnHit) {
            mother.setAttached(PREGNANCY, p.withWorse());
        }
    }

    public static void tickPregnancy(ServerLevel level, Mob mother) {
        Pregnancy p = mother.getAttached(PREGNANCY);
        if (p == null || level.getGameTime() < p.dueTime()) return;
        giveBirth(level, mother, p);
    }

    private static void giveBirth(ServerLevel level, Mob mother, Pregnancy p) {
        mother.removeAttached(PREGNANCY);
        if (!(mother instanceof AgeableMob am)) return;
        AgeableMob partner = am;
        if (p.fatherId().isPresent() && level.getEntity(p.fatherId().get()) instanceof AgeableMob dad && dad.isAlive()) {
            partner = dad;
        }
        AgeableMob baby = am.getBreedOffspring(level, partner);
        if (baby == null) return;
        baby.setBaby(true);
        baby.snapTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
        Colors.inherit(baby, mother, partner);
        {
            Mob child = baby;
            Genome mom = getOrCreate(mother);
            Taming motherTaming = mother.getAttached(TAMING);
            child.setAttached(TAMING, Taming.ownedRecord().withOwner(
                    motherTaming == null ? null : motherTaming.owner().orElse(null)));
            child.setAttached(GENOME, Genome.breed(mom, p.father(), ThreadLocalRandom.current(), p.worse(),
                    displayName(mother), p.fatherName()));
        }
        level.addFreshEntityWithPassengers(baby);
        level.broadcastEntityEvent(mother, (byte) 18);

        am.setAge(Math.max(0, am.getAge()) + IcBreedConfig.get().postBirthCooldownSeconds * 20);

        if (mother.hasCustomName()) {
            Component msg = Component.literal(mother.getCustomName().getString() + " has given birth!");
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(mother) < 128.0 * 128.0) player.sendSystemMessage(msg);
            }
        }
    }

    private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_VALUE;
    private static final AttributeModifier.Operation MUL = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

    public static void applyAttributes(Mob e, Genome g, boolean heal) {
        Effects fx = effects(e);
        if (fx == null) fx = g.effects();
        set(e, Attributes.MAX_HEALTH, "health_pct", clamp(fx.healthPct, -0.9, 10), MUL);
        set(e, Attributes.MAX_HEALTH, "health_flat", fx.healthFlat, ADD);
        set(e, Attributes.ATTACK_DAMAGE, "damage_pct", clamp(fx.damagePct, -0.9, 10), MUL);
        set(e, Attributes.ATTACK_DAMAGE, "damage_flat", fx.damageFlat, ADD);
        set(e, Attributes.MOVEMENT_SPEED, "speed_pct", clamp(fx.speedPct, -0.9, 10), MUL);
        set(e, Attributes.FLYING_SPEED, "fly_pct", clamp(fx.flySpeedPct, -0.9, 10), MUL);
        set(e, Attributes.JUMP_STRENGTH, "jump_pct", clamp(fx.jumpPct, -0.9, 10), MUL);
        set(e, Attributes.SCALE, "scale_pct", clamp(fx.scalePct, -0.9, 10), MUL);
        set(e, Attributes.KNOCKBACK_RESISTANCE, "knockback", Math.max(0, fx.knockbackRes), ADD);
        set(e, Attributes.ARMOR, "armor", fx.armorFlat, ADD);
        set(e, Attributes.FALL_DAMAGE_MULTIPLIER, "fall", clamp(fx.fallTakenPct, -0.95, 5), ADD);

        if (heal) e.setHealth(e.getMaxHealth());
        else if (e.getHealth() > e.getMaxHealth()) e.setHealth(e.getMaxHealth());
    }

    private static void set(Mob e, Holder<Attribute> attr, String name, double value,
                            AttributeModifier.Operation op) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst == null) return;
        Identifier id = Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, name);
        inst.removeModifier(id);
        if (Math.abs(value) > 1.0E-6) inst.addTransientModifier(new AttributeModifier(id, value, op));
    }

    public static void fed(Mob animal, java.util.UUID player) {
        if (!supports(animal) || animal.level().isClientSide()) return;
        Effects fx = effects(animal);
        addXp(animal, (int) Math.round(XP_FEED * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
        com.iiampanda.icbreed.taming.TamingLogic.onFed(animal, player);
        com.iiampanda.icbreed.traits.Starvation.fed(animal);
    }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
