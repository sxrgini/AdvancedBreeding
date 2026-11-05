package com.iiampanda.icbreed.genetics;

public enum Stat {
    HEALTH_PCT("health_pct", true, (e, v) -> e.healthPct += v, e -> e.healthPct),
    HEALTH_FLAT("health_flat", false, (e, v) -> e.healthFlat += v, e -> e.healthFlat),
    DAMAGE_PCT("damage_pct", true, (e, v) -> e.damagePct += v, e -> e.damagePct),
    DAMAGE_FLAT("damage_flat", false, (e, v) -> e.damageFlat += v, e -> e.damageFlat),
    SPEED_PCT("speed_pct", true, (e, v) -> e.speedPct += v, e -> e.speedPct),
    JUMP_PCT("jump_pct", true, (e, v) -> e.jumpPct += v, e -> e.jumpPct),
    SCALE_PCT("scale_pct", true, (e, v) -> e.scalePct += v, e -> e.scalePct),
    KNOCKBACK("knockback", true, (e, v) -> e.knockbackRes += v, e -> e.knockbackRes),
    DAMAGE_TAKEN("damage_taken", true, (e, v) -> e.damageTakenPct += v, e -> e.damageTakenPct),
    THREAT("threat", true, (e, v) -> e.threatPct += v, e -> e.threatPct),
    XP("xp", true, (e, v) -> e.xpPct += v, e -> e.xpPct),
    FERTILITY("fertility", true, (e, v) -> e.fertilityPct += v, e -> e.fertilityPct),
    PHENO_MUTATION("pheno_mutation", true, (e, v) -> e.phenoMutation += v, e -> e.phenoMutation),
    MUTATION("mutation", true, (e, v) -> e.mutationBonus += v, e -> e.mutationBonus),
    LEECH("leech", true, (e, v) -> e.leechChance += v, e -> e.leechChance),
    BLEED("bleed", true, (e, v) -> e.bleedChance += v, e -> e.bleedChance),
    ELEMENT_TAKEN("element_taken", true, (e, v) -> e.elementTakenPct += v, e -> e.elementTakenPct),
    GROWTH("growth", true, (e, v) -> e.growthPct += v, e -> e.growthPct),
    ARMOR("armor", false, (e, v) -> e.armorFlat += v, e -> e.armorFlat),
    REGEN("regen", false, (e, v) -> e.regenPerSec += v, e -> e.regenPerSec),
    FALL_TAKEN("fall_taken", true, (e, v) -> e.fallTakenPct += v, e -> e.fallTakenPct),
    GESTATION("gestation", true, (e, v) -> e.gestationPct += v, e -> e.gestationPct),
    FED_DURATION("fed_duration", true, (e, v) -> e.fedDurationPct += v, e -> e.fedDurationPct),
    FEED_XP("feed_xp", true, (e, v) -> e.feedXpPct += v, e -> e.feedXpPct),
    EXTRA_DROP("extra_drop", true, (e, v) -> e.extraDropChance += v, e -> e.extraDropChance),
    MARK("mark", false, (e, v) -> e.markSeconds += v, e -> e.markSeconds),
    STUN("stun", true, (e, v) -> e.stunChance += v, e -> e.stunChance),
    CHARGE("charge", false, (e, v) -> e.chargeSeconds += v, e -> e.chargeSeconds),
    CALMING("calming", true, (e, v) -> e.calmingPct += v, e -> e.calmingPct),
    UNSEAT("unseat", true, (e, v) -> e.unseatChance += v, e -> e.unseatChance),
    BITE("bite", false, (e, v) -> e.poisonBiteSeconds += v, e -> e.poisonBiteSeconds),
    AURA("aura", true, (e, v) -> e.auraPct += v, e -> e.auraPct),
    PRODUCTION("production", false, (e, v) -> e.productionRank += v, e -> e.productionRank),
    COORD("coord", true, (e, v) -> e.coordPct += v, e -> e.coordPct),
    STEW("stew", false, (e, v) -> e.stewRank += v, e -> e.stewRank),
    SAND_SPEED("sand_speed", true, (e, v) -> e.sandPct += v, e -> e.sandPct),
    HOP_AURA("hop_aura", false, (e, v) -> e.hopAura += v, e -> e.hopAura),
    REGEN_AURA("regen_aura", false, (e, v) -> e.regenAura += v, e -> e.regenAura),
    SAT_AURA("sat_aura", false, (e, v) -> e.satAura += v, e -> e.satAura),
    NINE_LIVES("nine_lives", false, (e, v) -> e.nineLives += v, e -> e.nineLives),
    CLING("cling", false, (e, v) -> e.cling += v, e -> e.cling),
    FEATHERS("feathers", false, (e, v) -> e.feathers += v, e -> e.feathers),
    NUGGETS("nuggets", false, (e, v) -> e.nuggets += v, e -> e.nuggets),
    HAT("hat", false, (e, v) -> e.hat += v, e -> e.hat),
    FERAL("feral", false, (e, v) -> e.feral += v, e -> e.feral),
    RIDEABLE("rideable", false, (e, v) -> e.rideable += v, e -> e.rideable),
    FIRE_AURA("fire_aura", false, (e, v) -> e.fireAura += v, e -> e.fireAura),
    LAVA_SPEED("lava_speed", true, (e, v) -> e.lavaPct += v, e -> e.lavaPct),
    CAPTAIN("captain", false, (e, v) -> e.captain += v, e -> e.captain),
    FLY_SPEED("fly_speed", true, (e, v) -> e.flySpeedPct += v, e -> e.flySpeedPct),
    BUILDABLE("buildable", false, (e, v) -> e.buildable += v, e -> e.buildable),
    WATER_BREATH("water_breath", false, (e, v) -> e.waterBreath += v, e -> e.waterBreath),
    WATER_VISION("water_vision", false, (e, v) -> e.waterVision += v, e -> e.waterVision),
    POLLINATION("pollination", false, (e, v) -> e.pollination += v, e -> e.pollination),
    HONEY_FAST("honey_fast", false, (e, v) -> e.honeyFast += v, e -> e.honeyFast),
    HONEY_BLOCK("honey_block", false, (e, v) -> e.honeyBlock += v, e -> e.honeyBlock),
    SHOULDER_PAL("shoulder_pal", false, (e, v) -> e.shoulderPal += v, e -> e.shoulderPal),
    ELYTRA_BOOST("elytra_boost", false, (e, v) -> e.elytraBoost += v, e -> e.elytraBoost),
    POLLY_PICKUP("polly_pickup", false, (e, v) -> e.pollyPickup += v, e -> e.pollyPickup),
    BAT_GLIDER("bat_glider", false, (e, v) -> e.batGlider += v, e -> e.batGlider),
    FOOD_USE("food_use", true, (e, v) -> e.foodUse += v, e -> e.foodUse),
    SAT_RATE("sat_rate", true, (e, v) -> e.satRate += v, e -> e.satRate),
    PHYS_RES("phys_res", true, (e, v) -> e.physRes += v, e -> e.physRes),
    REGEN_PCT("regen_pct", true, (e, v) -> e.regenPct += v, e -> e.regenPct),
    POISON_TOUCH("poison_touch", false, (e, v) -> e.poisonTouch += v, e -> e.poisonTouch),
    LLAMA_WOOL("llama_wool", false, (e, v) -> e.llamaWool += v, e -> e.llamaWool);

    public interface Applier { void apply(Effects e, double v); }
    public interface Getter { double get(Effects e); }

    public final String id;
    public final boolean percent;
    private final Applier applier;
    private final Getter getter;

    Stat(String id, boolean percent, Applier applier, Getter getter) {
        this.getter = getter;
        this.id = id;
        this.percent = percent;
        this.applier = applier;
    }

    public double get(Effects e) {
        return getter.get(e);
    }

    public void apply(Effects e, double v) {
        applier.apply(e, v);
    }

    public static Stat byId(String id) {
        for (Stat s : values()) if (s.id.equals(id)) return s;
        return null;
    }

    public String nameKey() {
        return "advbreeding.stat." + id;
    }

    public String format(double v) {
        String sign = v >= 0 ? "+" : "-";
        double a = Math.abs(v);
        if (percent) {
            double p = a * 100.0;
            return sign + (Math.abs(p - Math.rint(p)) < 0.05 ? String.valueOf((long) Math.rint(p)) : String.format("%.1f", p)) + "%";
        }
        return sign + (Math.abs(a - Math.rint(a)) < 0.005 ? String.valueOf((long) Math.rint(a)) : String.format("%.2f", a));
    }
}
