package com.iiampanda.icbreed.traits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iiampanda.icbreed.IcBreed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.iiampanda.icbreed.genetics.Stat;
import com.iiampanda.icbreed.traits.TraitDef.Mod;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class TraitTrees {
    public static final class Tree {
        public final String species;
        public final List<TraitDef> nodes;
        private final Map<String, TraitDef> byId = new LinkedHashMap<>();

        Tree(String species, List<TraitDef> nodes) {
            this.species = species;
            this.nodes = nodes;
            for (TraitDef d : nodes) byId.put(d.id(), d);
        }

        public TraitDef get(String id) {
            return byId.get(id);
        }

        public String rootId() {
            return nodes.get(0).id();
        }
    }

    private static volatile Map<String, Tree> TREES = new LinkedHashMap<>();
    private static Map<String, Tree> target = TREES;
    private static final Map<String, TraitDef> BASE = new LinkedHashMap<>();
    private static volatile Map<String, JsonObject> OVERRIDES = Map.of();
    private static volatile Map<String, List<TraitDef>> EXTRAS = Map.of();
    private static volatile Map<String, Tree> MERGED = new ConcurrentHashMap<>();

    private TraitTrees() {
    }

    private static Mod m(Stat s, double v) {
        return new Mod(s, v);
    }

    private static TraitDef n(String id, int max, String parent, int col, int row, Item icon, Mod... mods) {
        return new TraitDef(id, max, parent, null, col, row, icon, List.of(mods));
    }

    private static TraitDef n3(String id, int max, String parent, int cost, int col, int row, Item icon, Mod... mods) {
        return new TraitDef(id, max, parent, null, col, row, icon, List.of(mods), cost);
    }

    private static TraitDef n2(String id, int max, String parent, String parent2, int col, int row, Item icon, Mod... mods) {
        return new TraitDef(id, max, parent, parent2, col, row, icon, List.of(mods));
    }

    private static final Map<String, boolean[]> RIDERS = Map.ofEntries(
            Map.entry("camel", new boolean[] {true, true}),
            Map.entry("horse", new boolean[] {true, true}),
            Map.entry("creature_rideable", new boolean[] {true, true}),
            Map.entry("llama", new boolean[] {true, true}),
            Map.entry("pig", new boolean[] {true, false}),
            Map.entry("sniffer", new boolean[] {true, true}),
            Map.entry("panda", new boolean[] {true, true}),
            Map.entry("polar_bear", new boolean[] {true, true}),
            Map.entry("hoglin", new boolean[] {true, true}),
            Map.entry("nautilus", new boolean[] {true, false}),
            Map.entry("strider", new boolean[] {true, false}),
            Map.entry("happy_ghast", new boolean[] {false, false}));

    private static final java.util.Set<String> TWO_POINT = java.util.Set.of("rideable", "unseat", "water_breather", "underwater_vision", "rip_and_tear", "nine_lives", "cat_scratches", "axolotl_hat", "regen_aura");

    private static final java.util.Set<String> PHENO = java.util.Set.of("sheep", "cow", "mooshroom", "pig", "chicken", "cat", "wolf", "rabbit", "llama", "fox", "parrot", "axolotl", "horse", "frog");

    private static void addRide(List<TraitDef> l, String species) {
        boolean[] f = RIDERS.get(species);
        if (f == null) return;
        l.add(n("mount_armor", 3, "hydration_elasticity", 3, 0, Items.IRON_HORSE_ARMOR, m(Stat.ARMOR, 1.5)));
        String last = "mount_armor";
        int col = 4;
        if (f[0]) {
            l.add(n("mount_speed", 3, last, col++, 0, Items.GOLDEN_CARROT, m(Stat.SPEED_PCT, 0.05)));
            last = "mount_speed";
        }
        if (f[1]) l.add(n("mount_jump", 3, last, col, 0, Items.RABBIT_FOOT, m(Stat.JUMP_PCT, 0.05)));
    }

    private static Tree build(String species, Item rootIcon, TraitDef... speciesNodes) {
        return build(species, rootIcon, 0, speciesNodes);
    }

    private static Tree build(String species, Item rootIcon, int shift, TraitDef... speciesNodes) {
        List<TraitDef> l = new ArrayList<>();
        l.add(n(species, 1, null, 0, 3, rootIcon));
        l.add(n("wild_fortitude", 4, species, 1, 1, Items.GOLDEN_APPLE, m(Stat.HEALTH_PCT, 0.04)));
        l.add(n("tough_as_leather", 4, species, 1, 2, Items.LEATHER, m(Stat.DAMAGE_TAKEN, -0.03)));
        l.add(n("nurtured_recovery", 4, species, 1, 4, Items.GLISTERING_MELON_SLICE, m(Stat.REGEN, 0.05)));
        l.add(n("metabolic_preservation", 4, species, 1, 5, Items.BREAD, m(Stat.FERTILITY, 0.10)));
        l.add(n("hydration_elasticity", 4, "wild_fortitude", 2, 0, Items.WATER_BUCKET, m(Stat.ELEMENT_TAKEN, -0.06)));
        l.add(n("perfect_pasturage", 4, "nurtured_recovery", 2, 4, Items.HAY_BLOCK, m(Stat.GESTATION, 0.06)));
        l.add(n("large_stomach", 4, "metabolic_preservation", 2, 6, Items.COOKED_BEEF, m(Stat.FED_DURATION, 0.25)));
        for (TraitDef d : speciesNodes) l.add(d);
        if (PHENO.contains(species)) l.add(n3("phenotype_mutation", 3, "metabolic_preservation", 2, 1, 6, Items.NETHER_STAR, m(Stat.PHENO_MUTATION, 0.05)));
        addRide(l, species);
        List<TraitDef> moved = new ArrayList<>();
        for (TraitDef d : l) {
            int cost = TWO_POINT.contains(d.id()) ? 2 : d.cost();
            TraitDef built = new TraitDef(d.id(), d.maxRank(), d.parent(), d.parent2(), d.col() + shift, d.row(), d.icon(), d.mods(), cost);
            BASE.putIfAbsent(built.id(), built);
            moved.add(applyOverride(built));
        }
        l = moved;
        Tree t = new Tree(species, l);
        target.put(species, t);
        return t;
    }

    private static void buildAll() {
        build("wolf", Items.BONE,
                n("natural_ferocity", 4, "wolf", 1, 3, Items.IRON_SWORD, m(Stat.DAMAGE_PCT, 0.05)),
                n("coordinated_attacks", 3, "natural_ferocity", 2, 3, Items.GOAT_HORN, m(Stat.COORD, 0.04)),
                n("rip_and_tear", 1, "tough_as_leather", 2, 2, Items.POTION, m(Stat.BITE, 10)),
                n("endless_hunger", 3, "rip_and_tear", 3, 2, Items.ROTTEN_FLESH, m(Stat.LEECH, 0.08)),
                n("hunters_mark", 3, "wild_fortitude", 2, 1, Items.GLOWSTONE_DUST, m(Stat.MARK, 5)));

        build("sheep", Items.WHITE_WOOL,
                n("calming_baas", 3, "sheep", 1, 3, Items.NOTE_BLOCK, m(Stat.THREAT, -0.08)),
                n("calming_presence", 4, "calming_baas", 2, 3, Items.CAMPFIRE, m(Stat.CALMING, -0.10)),
                n("thick_hide", 3, "tough_as_leather", 2, 2, Items.IRON_CHESTPLATE, m(Stat.ARMOR, 1)),
                n("healthy_mutton", 3, "wild_fortitude", 2, 1, Items.COOKED_MUTTON, m(Stat.EXTRA_DROP, 0.20)),
                n("fed_well", 3, "healthy_mutton", 3, 1, Items.WHEAT, m(Stat.FEED_XP, 0.25)),
                n("wool_production", 3, "metabolic_preservation", 2, 5, Items.SHEARS, m(Stat.PRODUCTION, 1)));

        build("cow", Items.MILK_BUCKET,
                n("calming_moos", 3, "cow", 1, 3, Items.NOTE_BLOCK, m(Stat.THREAT, -0.08)),
                n("calming_presence", 4, "calming_moos", 2, 3, Items.CAMPFIRE, m(Stat.CALMING, -0.10)),
                n("thick_hide", 3, "tough_as_leather", 2, 2, Items.IRON_CHESTPLATE, m(Stat.ARMOR, 1)),
                n("holstein_harvest", 3, "wild_fortitude", 2, 1, Items.COOKED_BEEF, m(Stat.EXTRA_DROP, 0.20)),
                n("fed_well", 3, "holstein_harvest", 3, 1, Items.WHEAT, m(Stat.FEED_XP, 0.25)),
                n("milk_production", 3, "metabolic_preservation", 2, 5, Items.BUCKET, m(Stat.PRODUCTION, 1)));

        Tree cowTree = target.get("cow");
        List<TraitDef> mooshroomNodes = new ArrayList<>(cowTree.nodes);
        mooshroomNodes.add(applyOverride(baseOf(n("very_suspicious", 3, "milk_production", 3, 5, Items.SUSPICIOUS_STEW, m(Stat.STEW, 1)))));
        target.put("mooshroom", new Tree("cow", mooshroomNodes));

        build("pig", Items.CARROT_ON_A_STICK,
                n("natural_ferocity", 4, "pig", 1, 3, Items.IRON_SWORD, m(Stat.DAMAGE_PCT, 0.05)),
                n("charge", 3, "natural_ferocity", 2, 3, Items.SUGAR, m(Stat.SPEED_PCT, 0.05)),
                n("rip_and_tear", 1, "tough_as_leather", 2, 2, Items.POTION, m(Stat.BITE, 10)),
                n("endless_hunger", 3, "rip_and_tear", 3, 2, Items.ROTTEN_FLESH, m(Stat.LEECH, 0.08)),
                n("thump_thump", 3, "wild_fortitude", 2, 1, Items.MACE, m(Stat.STUN, 0.10)),
                n("hunters_mark", 3, "thump_thump", 3, 1, Items.GLOWSTONE_DUST, m(Stat.MARK, 5)));

        if (com.iiampanda.icbreed.compat.Backport.spears()) {
            build("horse", Items.SADDLE,
                    n("unseat", 1, "tough_as_leather", 2, 2, com.iiampanda.icbreed.compat.Backport.spearIcon(), m(Stat.UNSEAT, 0.25)),
                    n2("saddle_bags", 1, "unseat", "mount_jump", 3, 2, Items.BARREL),
                    n3("shulker_bags", 1, "saddle_bags", 2, 4, 2, Items.SHULKER_BOX));
        } else {
            build("horse", Items.SADDLE,
                    n("saddle_bags", 1, "mount_jump", 3, 2, Items.BARREL),
                    n3("shulker_bags", 1, "saddle_bags", 2, 4, 2, Items.SHULKER_BOX));
        }

        build("chicken", Items.FEATHER,
                n("egg_drops", 3, "metabolic_preservation", 2, 5, Items.EGG, m(Stat.PRODUCTION, 1)),
                n("shearable_feathers", 3, "egg_drops", 3, 5, Items.SHEARS, m(Stat.FEATHERS, 1)));
        build("armadillo", Items.ARMADILLO_SCUTE,
                n("scute_drops", 3, "metabolic_preservation", 2, 5, Items.ARMADILLO_SCUTE, m(Stat.PRODUCTION, 1)));

        build("axolotl", Items.TROPICAL_FISH_BUCKET,
                n("regen_aura", 1, "axolotl", 1, 3, Items.POTION, m(Stat.REGEN_AURA, 1)),
                n("axolotl_hat", 1, "axolotl", 1, 0, Items.SKELETON_SKULL, m(Stat.HAT, 1)));

        build("bee", Items.HONEYCOMB,
                n("pollination", 2, "bee", 1, 3, Items.SUNFLOWER, m(Stat.POLLINATION, 1)),
                n("increased_honey_production", 1, "pollination", 2, 3, Items.HONEY_BOTTLE, m(Stat.HONEY_FAST, 1)),
                n("honey_block_production", 1, "increased_honey_production", 3, 3, Items.HONEY_BLOCK, m(Stat.HONEY_BLOCK, 1)));

        build("parrot", Items.WHEAT_SEEDS,
                n("shoulder_pal", 1, "parrot", 1, 3, Items.FEATHER, m(Stat.SHOULDER_PAL, 1)),
                new TraitDef("elytra_boost", 1, "shoulder_pal", null, 2, 3, Items.ELYTRA, List.of(m(Stat.ELYTRA_BOOST, 1)), 2),
                new TraitDef("polly_pickup", 2, "shoulder_pal", "tough_as_leather", 2, 2, Items.COOKIE, List.of(m(Stat.POLLY_PICKUP, 1)), 2));

        build("camel", Items.CACTUS,
                n("saturation_aura", 1, "camel", 1, 3, Items.CAMPFIRE, m(Stat.SAT_AURA, 1)),
                n("desert_walker", 3, "tough_as_leather", 2, 2, Items.SAND, m(Stat.SAND_SPEED, 0.10)));

        build("llama", Items.WHITE_CARPET,
                n("rideable", 1, "wild_fortitude", 2, 1, Items.SADDLE, m(Stat.RIDEABLE, 1)),
                n("llama_thick_hide", 3, "tough_as_leather", 2, 2, Items.WHITE_WOOL, m(Stat.LLAMA_WOOL, 1)));

        build("cat", Items.COD,
                n("nine_lives", 1, "cat", 1, 3, Items.TOTEM_OF_UNDYING, m(Stat.NINE_LIVES, 1)),
                n("cat_scratches", 1, "cat", 1, 0, Items.COBWEB, m(Stat.CLING, 1)));

        build("sniffer", Items.TORCHFLOWER_SEEDS,
                n("metal_scraps", 1, "sniffer", 1, 3, Items.GOLD_NUGGET, m(Stat.NUGGETS, 1)),
                n("rideable", 1, "wild_fortitude", 2, 1, Items.SADDLE, m(Stat.RIDEABLE, 1)));

        build("fox", Items.SWEET_BERRIES, 1,
                n("feral_side", 1, "fox", -1, 3, Items.IRON_SWORD, m(Stat.FERAL, 1)));

        build("panda", Items.BAMBOO, 1,
                n("feral_side", 1, "panda", -1, 3, Items.IRON_SWORD, m(Stat.FERAL, 1)),
                n("rideable", 1, "wild_fortitude", 2, 1, Items.SADDLE, m(Stat.RIDEABLE, 1)));

        build("polar_bear", Items.SALMON,
                n("natural_ferocity", 4, "polar_bear", 1, 3, Items.IRON_SWORD, m(Stat.DAMAGE_PCT, 0.05)),
                n("rideable", 1, "wild_fortitude", 2, 1, Items.SADDLE, m(Stat.RIDEABLE, 1)));

        build("hoglin", Items.PORKCHOP,
                n("natural_ferocity", 4, "hoglin", 1, 3, Items.IRON_SWORD, m(Stat.DAMAGE_PCT, 0.05)),
                n("rideable", 1, "wild_fortitude", 2, 1, Items.SADDLE, m(Stat.RIDEABLE, 1)));

        if (com.iiampanda.icbreed.compat.Backport.present()) {
            build("nautilus", Items.NAUTILUS_SHELL,
                    n("water_breather", 1, "wild_fortitude", 2, 1, Items.POTION, m(Stat.WATER_BREATH, 1)),
                    n("underwater_vision", 1, "water_breather", 3, 1, Items.POTION, m(Stat.WATER_VISION, 1)));
        }

        build("strider", Items.WARPED_FUNGUS_ON_A_STICK,
                n("fire_resistance_aura", 1, "strider", 1, 3, Items.POTION, m(Stat.FIRE_AURA, 1)),
                n("lava_walker", 3, "fire_resistance_aura", 2, 3, Items.LAVA_BUCKET, m(Stat.LAVA_SPEED, 0.08)),
                n("captain_now", 1, "lava_walker", 3, 3, Items.SADDLE, m(Stat.CAPTAIN, 1)));

        if (com.iiampanda.icbreed.compat.Backport.present()) {
            build("happy_ghast", Items.GHAST_TEAR,
                    n("buildable", 1, "happy_ghast", 1, 0, Items.OAK_LOG, m(Stat.BUILDABLE, 1)),
                    n("flying_speed", 3, "happy_ghast", 1, 3, Items.WIND_CHARGE, m(Stat.FLY_SPEED, 0.11)));
        }

        build("rabbit", Items.RABBIT_FOOT,
                n("hoppy_aura", 1, "wild_fortitude", 2, 1, Items.RABBIT_FOOT, m(Stat.HOP_AURA, 1)));
        build("frog", Items.LILY_PAD);
        build("creature", Items.OAK_SAPLING);
        build("creature_rideable", Items.OAK_SAPLING);
    }

    private static TraitDef baseOf(TraitDef d) {
        BASE.putIfAbsent(d.id(), d);
        return d;
    }

    private static void rebuild() {
        Map<String, Tree> next = new LinkedHashMap<>();
        target = next;
        buildAll();
        TREES = next;
        MERGED = new ConcurrentHashMap<>();
    }

    static {
        rebuild();
    }

    private static List<Mod> parseMods(JsonArray a) {
        List<Mod> out = new ArrayList<>();
        for (JsonElement el : a) {
            if (!el.isJsonObject()) continue;
            JsonObject j = el.getAsJsonObject();
            Stat stat = j.has("stat") ? Stat.byId(j.get("stat").getAsString()) : null;
            if (stat == null) {
                IcBreed.LOGGER.warn("Unknown trait stat: {}", j.get("stat"));
                continue;
            }
            out.add(new Mod(stat, j.has("per_rank") ? j.get("per_rank").getAsDouble() : 0));
        }
        return out;
    }

    private static TraitDef applyOverride(TraitDef d) {
        JsonObject o = OVERRIDES.get(d.id());
        TraitDef base = BASE.get(d.id());
        if (o == null || base == null) return d;
        int cost = d.cost();
        int max = d.maxRank();
        List<Mod> mods = d.mods();
        try {
            if (o.has("cost") && o.get("cost").getAsInt() != base.cost()) cost = Math.max(0, o.get("cost").getAsInt());
            if (o.has("max_rank") && o.get("max_rank").getAsInt() != base.maxRank()) max = Math.max(1, o.get("max_rank").getAsInt());
            if (o.has("mods") && o.get("mods").isJsonArray()) {
                List<Mod> parsed = parseMods(o.getAsJsonArray("mods"));
                if (!parsed.equals(base.mods())) mods = parsed;
            }
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Invalid override for trait {}", d.id(), e);
        }
        return new TraitDef(d.id(), max, d.parent(), d.parent2(), d.col(), d.row(), d.icon(), mods, cost, d.name(), d.description());
    }

    public static JsonObject defaultsJson() {
        JsonObject out = new JsonObject();
        for (TraitDef d : BASE.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("cost", d.cost());
            o.addProperty("max_rank", d.maxRank());
            JsonArray mods = new JsonArray();
            for (Mod m : d.mods()) {
                JsonObject j = new JsonObject();
                j.addProperty("stat", m.stat().id);
                j.addProperty("per_rank", m.perRank());
                mods.add(j);
            }
            o.add("mods", mods);
            out.add(d.id(), o);
        }
        return out;
    }

    private static TraitDef parseTrait(JsonObject j) {
        String id = j.get("id").getAsString();
        String parent = j.has("parent") && !j.get("parent").isJsonNull() ? j.get("parent").getAsString() : "wild_fortitude";
        String parent2 = j.has("parent2") && !j.get("parent2").isJsonNull() ? j.get("parent2").getAsString() : null;
        Item icon = Items.PAPER;
        if (j.has("icon")) {
            ResourceLocation iconId = ResourceLocation.tryParse(j.get("icon").getAsString());
            Item found = iconId == null ? null : BuiltInRegistries.ITEM.get(iconId);
            if (found == null || found == Items.AIR) IcBreed.LOGGER.warn("Unknown icon for trait {}: {}", id, j.get("icon"));
            else icon = found;
        }
        List<Mod> mods = j.has("mods") && j.get("mods").isJsonArray() ? parseMods(j.getAsJsonArray("mods")) : List.of();
        return new TraitDef(id, Math.max(1, j.has("max_rank") ? j.get("max_rank").getAsInt() : 1), parent, parent2,
                j.has("col") ? j.get("col").getAsInt() : 2, j.has("row") ? j.get("row").getAsInt() : 1, icon, mods,
                Math.max(0, j.has("cost") ? j.get("cost").getAsInt() : 1),
                j.has("name") ? j.get("name").getAsString() : null, j.has("description") ? j.get("description").getAsString() : null);
    }

    public static void configure(JsonObject traits, JsonArray mobTraits) {
        Map<String, JsonObject> overrides = new HashMap<>();
        for (Map.Entry<String, JsonElement> en : traits.entrySet()) {
            if (en.getValue().isJsonObject()) overrides.put(en.getKey(), en.getValue().getAsJsonObject());
        }
        Map<String, List<TraitDef>> extras = new HashMap<>();
        for (JsonElement fileEl : mobTraits) {
            if (!fileEl.isJsonObject()) continue;
            JsonObject file = fileEl.getAsJsonObject();
            List<String> entities = new ArrayList<>();
            if (file.has("entity")) entities.add(file.get("entity").getAsString());
            if (file.has("entities") && file.get("entities").isJsonArray()) {
                for (JsonElement e : file.getAsJsonArray("entities")) entities.add(e.getAsString());
            }
            List<TraitDef> defs = new ArrayList<>();
            if (file.has("traits") && file.get("traits").isJsonArray()) {
                for (JsonElement e : file.getAsJsonArray("traits")) {
                    try {
                        if (e.isJsonObject() && e.getAsJsonObject().has("id")) defs.add(parseTrait(e.getAsJsonObject()));
                    } catch (Exception ex) {
                        IcBreed.LOGGER.warn("Invalid mob trait entry", ex);
                    }
                }
            }
            for (String ent : entities) extras.computeIfAbsent(ent, k -> new ArrayList<>()).addAll(defs);
        }
        OVERRIDES = overrides;
        EXTRAS = extras;
        rebuild();
    }

    private static Tree merge(Tree base, List<TraitDef> extra) {
        List<TraitDef> nodes = new ArrayList<>(base.nodes);
        Map<String, TraitDef> ids = new HashMap<>();
        for (TraitDef d : nodes) ids.put(d.id(), d);
        for (TraitDef d : extra) {
            if (ids.containsKey(d.id())) continue;
            if (d.parent() != null && !ids.containsKey(d.parent()) || d.parent2() != null && !ids.containsKey(d.parent2())) {
                IcBreed.LOGGER.warn("Trait {} skipped: parent not found", d.id());
                continue;
            }
            nodes.add(d);
            ids.put(d.id(), d);
        }
        return new Tree(base.species, nodes);
    }

    private static Tree fallback(net.minecraft.world.entity.Mob mob) {
        return com.iiampanda.icbreed.api.AdvancedBreedingApi.isRideableType(mob) ? TREES.get("creature_rideable") : TREES.get("creature");
    }

    public static Tree forMob(net.minecraft.world.entity.Mob animal) {
        Tree base = baseMob(animal);
        Map<String, List<TraitDef>> extras = EXTRAS;
        if (extras.isEmpty()) return base;
        String key = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString();
        List<TraitDef> extra = extras.get(key);
        if (extra == null) return base;
        return MERGED.computeIfAbsent(key + "|" + base.species, k -> merge(base, extra));
    }

    private static Tree baseMob(net.minecraft.world.entity.Mob animal) {
        return animal instanceof Animal a ? forAnimal(a) : fallback(animal);
    }

    public static Tree forAnimal(Animal animal) {
        String compat = com.iiampanda.icbreed.compat.Compat.treeFor(animal);
        if (compat != null && TREES.containsKey(compat)) return TREES.get(compat);
        if (animal instanceof Wolf) return TREES.get("wolf");
        if (animal instanceof net.minecraft.world.entity.monster.Strider) return TREES.get("strider");
        if (animal instanceof net.minecraft.world.entity.animal.camel.Camel) return TREES.get("camel");
        if (animal instanceof net.minecraft.world.entity.animal.axolotl.Axolotl) return TREES.get("axolotl");
        if (animal instanceof net.minecraft.world.entity.animal.Parrot) return TREES.get("parrot");
        if (animal instanceof net.minecraft.world.entity.animal.Bee) return TREES.get("bee");
        if (animal instanceof net.minecraft.world.entity.animal.Cat
                || animal instanceof net.minecraft.world.entity.animal.Ocelot) return TREES.get("cat");
        if (animal instanceof net.minecraft.world.entity.animal.sniffer.Sniffer) return TREES.get("sniffer");
        if (animal instanceof net.minecraft.world.entity.animal.Fox) return TREES.get("fox");
        if (animal instanceof net.minecraft.world.entity.animal.Panda) return TREES.get("panda");
        if (animal instanceof net.minecraft.world.entity.animal.PolarBear) return TREES.get("polar_bear");
        if (animal instanceof net.minecraft.world.entity.monster.hoglin.Hoglin) return TREES.get("hoglin");
        if (animal instanceof Sheep) return TREES.get("sheep");
        if (animal instanceof net.minecraft.world.entity.animal.MushroomCow) return TREES.get("mooshroom");
        if (animal instanceof Cow) return TREES.get("cow");
        if (animal instanceof net.minecraft.world.entity.animal.horse.Llama) return TREES.get("llama");
        if (animal instanceof net.minecraft.world.entity.animal.horse.AbstractHorse) return TREES.get("horse");
        if (animal instanceof net.minecraft.world.entity.animal.Rabbit) return TREES.get("rabbit");
        if (animal instanceof net.minecraft.world.entity.animal.frog.Frog) return TREES.get("frog");
        if (animal instanceof Pig) return TREES.get("pig");
        if (animal instanceof net.minecraft.world.entity.animal.Chicken) return TREES.get("chicken");
        if (animal instanceof net.minecraft.world.entity.animal.armadillo.Armadillo) return TREES.get("armadillo");
        return fallback(animal);
    }

    public static Tree forEntity(net.minecraft.world.entity.Entity e) {
        return e instanceof net.minecraft.world.entity.Mob m ? forMob(m) : TREES.get("creature");
    }
}
