package com.iiampanda.icbreed.genetics;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

public record Taming(boolean tamed, boolean orphan, boolean led, long fedUntil, boolean pen, boolean bed,
                     boolean young, int startAge, Optional<UUID> owner) {
    public static final Codec<Taming> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("tamed", false).forGetter(Taming::tamed),
            Codec.BOOL.optionalFieldOf("orphan", false).forGetter(Taming::orphan),
            Codec.BOOL.optionalFieldOf("led", false).forGetter(Taming::led),
            Codec.LONG.optionalFieldOf("fedUntil", 0L).forGetter(Taming::fedUntil),
            Codec.BOOL.optionalFieldOf("pen", false).forGetter(Taming::pen),
            Codec.BOOL.optionalFieldOf("bed", false).forGetter(Taming::bed),
            Codec.BOOL.optionalFieldOf("young", false).forGetter(Taming::young),
            Codec.INT.optionalFieldOf("startAge", 0).forGetter(Taming::startAge),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(Taming::owner)
    ).apply(i, Taming::new));

    public static Taming wild() {
        return new Taming(false, false, false, 0L, false, false, false, 0, Optional.empty());
    }

    public static Taming ownedRecord() {
        return new Taming(true, false, false, 0L, false, false, true, 0, Optional.empty());
    }

    public Taming withOrphan() {
        return new Taming(tamed, true, led, fedUntil, pen, bed, young, startAge, owner);
    }

    public Taming withLed() {
        return new Taming(tamed, orphan, true, fedUntil, pen, bed, young, startAge, owner);
    }

    public Taming withFed(long until, UUID who) {
        return new Taming(tamed, orphan, led, until, pen, bed, young, startAge, Optional.ofNullable(who).or(() -> owner));
    }

    public Taming withYoung() {
        return new Taming(tamed, orphan, led, fedUntil, pen, bed, true, startAge, owner);
    }

    public Taming withOwner(UUID who) {
        return new Taming(tamed, orphan, led, fedUntil, pen, bed, young, startAge, Optional.ofNullable(who));
    }

    public Taming withStart(int age) {
        return new Taming(tamed, orphan, led, fedUntil, pen, bed, young, Math.min(age, -1), owner);
    }

    public boolean started() {
        return startAge < 0;
    }

    public int progress(int age) {
        if (!started()) return 0;
        double p = (age - startAge) / (double) (-startAge);
        return (int) Math.round(100.0 * Math.max(0.0, Math.min(1.0, p)));
    }

    public Taming withChecks(boolean pen, boolean bed) {
        return new Taming(tamed, orphan, led, fedUntil, pen, bed, young, startAge, owner);
    }

    public Taming asTamed() {
        return new Taming(true, orphan, led, fedUntil, pen, bed, young, startAge, owner);
    }

    public boolean fed(long gameTime) {
        return gameTime < fedUntil;
    }
}
