package com.iiampanda.icbreed.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Bloodline;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.iiampanda.icbreed.genetics.Genotype;
import com.iiampanda.icbreed.genetics.Stat;
import com.iiampanda.icbreed.net.RulesSyncPayload;
import com.iiampanda.icbreed.traits.TraitTrees;
import com.iiampanda.icbreed.platform.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;

public final class Rules {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COMPACT = new Gson();
    private static volatile String snapshot = "";
    private static Object lastManager;
    private static boolean scanned;

    private Rules() {
    }

    public static void ensureLoaded(MinecraftServer server) {
        if (lastManager != null || server == null) return;
        lastManager = server.getResourceManager();
        reload(server, false);
    }

    public static void tick(MinecraftServer server) {
        Object manager = server.getResourceManager();
        boolean changed = manager != lastManager;
        if (!changed && scanned) return;
        lastManager = manager;
        scanned = true;
        reload(server, true);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) sendTo(p);
    }

    public static void sendTo(ServerPlayer player) {
        String s = snapshot;
        if (!s.isEmpty()) ServerPlayNetworking.send(player, new RulesSyncPayload(s));
    }

    public static void applyRemote(String json) {
        try {
            apply(COMPACT.fromJson(json, JsonObject.class));
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Could not apply synced rules", e);
        }
    }

    private static Path dir() {
        Path p = IcBreedConfig.configDir().resolve("advbreeding");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            IcBreed.LOGGER.warn("Could not create config folder", e);
        }
        return p;
    }

    private static JsonObject file(String name, JsonObject defaults) {
        Path path = dir().resolve(name);
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                JsonObject read = PRETTY.fromJson(r, JsonObject.class);
                if (read != null) return read;
            } catch (Exception e) {
                IcBreed.LOGGER.warn("Could not read {}, using defaults", name, e);
            }
            return defaults;
        }
        try (Writer w = Files.newBufferedWriter(path)) {
            PRETTY.toJson(defaults, w);
        } catch (IOException e) {
            IcBreed.LOGGER.warn("Could not write {}", name, e);
        }
        return defaults;
    }

    private static void reload(MinecraftServer server, boolean scan) {
        JsonObject root = new JsonObject();
        root.add("bloodlines", file("bloodlines.json", bloodlineDefaults()));
        root.add("genotypes", file("genotypes.json", genotypeDefaults()));
        root.add("xp", file("xp.json", xpDefaults()));
        root.add("traits", file("traits.json", traitDefaults()));
        root.add("mob_traits", readDatapacks(server));
        root.add("modded", ModdedMobs.scan(server, dir().resolve("modded_mobs.json"), scan));
        apply(root);
        snapshot = COMPACT.toJson(root);
    }

    private static JsonObject bloodlineDefaults() {
        JsonObject o = new JsonObject();
        for (Bloodline b : Bloodline.ALL) {
            JsonObject e = new JsonObject();
            e.addProperty("weight", b.defaultWeight);
            JsonArray fx = new JsonArray();
            for (Bloodline.Mod m : b.defaultMods) {
                JsonObject j = new JsonObject();
                j.addProperty("stat", m.stat().id);
                j.addProperty("fixed", m.fixed());
                j.addProperty("per_level", m.perLevel());
                j.addProperty("every", m.every());
                fx.add(j);
            }
            e.add("effects", fx);
            o.add(b.id, e);
        }
        return o;
    }

    private static JsonObject genotypeDefaults() {
        JsonObject o = new JsonObject();
        for (Genotype g : Genotype.ALL) o.addProperty(g.id, g.defaultPerLevel);
        return o;
    }

    private static JsonObject xpDefaults() {
        JsonObject o = new JsonObject();
        o.addProperty("feed", 2);
        o.addProperty("breed", 50);
        o.addProperty("birth", 25);
        o.addProperty("kill", 25);
        o.addProperty("produce", 20);
        JsonArray costs = new JsonArray();
        int[] def = Genome.defaultCosts();
        for (int l = 1; l <= Genome.MAX_LEVEL; l++) costs.add(def[l]);
        o.add("level_costs", costs);
        return o;
    }

    private static JsonObject traitDefaults() {
        return TraitTrees.defaultsJson();
    }

    private static JsonArray readDatapacks(MinecraftServer server) {
        JsonArray out = new JsonArray();
        Map<ResourceLocation, Resource> found = server.getResourceManager().listResources("advbreeding/mob_traits", id -> id.getPath().endsWith(".json"));
        List<ResourceLocation> ids = new ArrayList<>(found.keySet());
        ids.sort(null);
        for (ResourceLocation id : ids) {
            try (Reader r = found.get(id).openAsReader()) {
                JsonElement el = COMPACT.fromJson(r, JsonElement.class);
                if (el != null && el.isJsonObject()) out.add(el);
            } catch (Exception e) {
                IcBreed.LOGGER.warn("Could not read mob traits file {}", id, e);
            }
        }
        return out;
    }

    private static double num(JsonObject o, String key, double def) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsDouble() : def;
    }

    private static void apply(JsonObject root) {
        try {
            applyBloodlines(root.has("bloodlines") ? root.getAsJsonObject("bloodlines") : new JsonObject());
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Invalid bloodlines.json", e);
        }
        try {
            JsonObject g = root.has("genotypes") ? root.getAsJsonObject("genotypes") : new JsonObject();
            for (Genotype t : Genotype.ALL) t.perLevel = num(g, t.id, t.defaultPerLevel);
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Invalid genotypes.json", e);
        }
        try {
            applyXp(root.has("xp") ? root.getAsJsonObject("xp") : new JsonObject());
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Invalid xp.json", e);
        }
        try {
            TraitTrees.configure(root.has("traits") ? root.getAsJsonObject("traits") : new JsonObject(),
                    root.has("mob_traits") ? root.getAsJsonArray("mob_traits") : new JsonArray());
        } catch (Exception e) {
            IcBreed.LOGGER.warn("Invalid traits", e);
        }
        ModdedMobs.apply(root.has("modded") ? root.getAsJsonObject("modded") : null);
    }

    private static void applyBloodlines(JsonObject o) {
        for (Bloodline b : Bloodline.ALL) {
            if (!o.has(b.id) || !o.get(b.id).isJsonObject()) {
                b.configure(b.defaultWeight, b.defaultMods);
                continue;
            }
            JsonObject e = o.getAsJsonObject(b.id);
            int weight = (int) Math.max(0, num(e, "weight", b.defaultWeight));
            List<Bloodline.Mod> mods = new ArrayList<>();
            if (e.has("effects") && e.get("effects").isJsonArray()) {
                for (JsonElement el : e.getAsJsonArray("effects")) {
                    if (!el.isJsonObject()) continue;
                    JsonObject j = el.getAsJsonObject();
                    Stat stat = j.has("stat") ? Stat.byId(j.get("stat").getAsString()) : null;
                    if (stat == null) {
                        IcBreed.LOGGER.warn("Unknown stat in bloodline {}: {}", b.id, j.get("stat"));
                        continue;
                    }
                    mods.add(new Bloodline.Mod(stat, num(j, "fixed", 0), num(j, "per_level", 0), (int) Math.max(1, num(j, "every", 1))));
                }
            } else {
                b.configure(weight, b.defaultMods);
                continue;
            }
            b.configure(weight, mods.toArray(new Bloodline.Mod[0]));
        }
        Bloodline.recompute();
    }

    private static void applyXp(JsonObject o) {
        Genetics.XP_FEED = (int) num(o, "feed", 2);
        Genetics.XP_BREED = (int) num(o, "breed", 50);
        Genetics.XP_BIRTH = (int) num(o, "birth", 25);
        Genetics.XP_KILL = (int) num(o, "kill", 25);
        Genetics.XP_PRODUCE = (int) num(o, "produce", 20);
        int[] costs = Genome.defaultCosts();
        if (o.has("level_costs") && o.get("level_costs").isJsonArray()) {
            JsonArray a = o.getAsJsonArray("level_costs");
            int last = costs[1];
            for (int l = 1; l <= Genome.MAX_LEVEL; l++) {
                if (l - 1 < a.size()) last = Math.max(1, a.get(l - 1).getAsInt());
                costs[l] = last;
            }
        }
        Genome.LEVEL_COSTS = costs;
    }
}
