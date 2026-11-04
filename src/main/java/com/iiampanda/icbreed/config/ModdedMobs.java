package com.iiampanda.icbreed.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.genetics.Ages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ModdedMobs {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Set<String> disabled = Set.of();
    private static volatile Map<String, Boolean> rideable = Map.of();

    private ModdedMobs() {
    }

    private static String key(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    public static boolean enabled(Entity e) {
        Set<String> d = disabled;
        return d.isEmpty() || !d.contains(key(e.getType()));
    }

    public static Boolean rideableOverride(EntityType<?> type) {
        Map<String, Boolean> r = rideable;
        return r.isEmpty() ? null : r.get(key(type));
    }

    static JsonObject scan(MinecraftServer server, Path file, boolean doScan) {
        JsonObject root = new JsonObject();
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                JsonObject read = GSON.fromJson(r, JsonObject.class);
                if (read != null) root = read;
            } catch (Exception e) {
                IcBreed.LOGGER.warn("Could not read modded_mobs.json", e);
            }
        }
        JsonObject mobs = root.has("mobs") && root.get("mobs").isJsonObject() ? root.getAsJsonObject("mobs") : new JsonObject();
        Set<String> ignored = new HashSet<>();
        if (root.has("ignored") && root.get("ignored").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("ignored")) ignored.add(el.getAsString());
        }
        ServerLevel level = doScan ? server.overworld() : null;
        boolean changed = doScan && !Files.exists(file);
        for (ResourceLocation id : doScan && level != null ? BuiltInRegistries.ENTITY_TYPE.keySet() : java.util.Set.<ResourceLocation>of()) {
            String ns = id.getNamespace();
            if (ns.equals("minecraft") || ns.equals(IcBreed.MOD_ID)) continue;
            String name = id.toString();
            if (mobs.has(name) || ignored.contains(name)) continue;
            changed = true;
            boolean supported = false;
            try {
                Entity e = BuiltInRegistries.ENTITY_TYPE.get(id).create(level);
                supported = e != null && Ages.supportsType(e);
                if (e != null) e.discard();
            } catch (Throwable t) {
                supported = false;
            }
            if (supported) {
                JsonObject entry = new JsonObject();
                entry.addProperty("enabled", true);
                entry.addProperty("rideable", "auto");
                mobs.add(name, entry);
            } else {
                ignored.add(name);
            }
        }
        JsonArray ign = new JsonArray();
        ignored.stream().sorted().forEach(ign::add);
        root.add("mobs", mobs);
        root.add("ignored", ign);
        if (changed) {
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(root, w);
            } catch (IOException e) {
                IcBreed.LOGGER.warn("Could not write modded_mobs.json", e);
            }
        }
        JsonObject out = new JsonObject();
        JsonArray off = new JsonArray();
        JsonObject ride = new JsonObject();
        for (Map.Entry<String, JsonElement> en : mobs.entrySet()) {
            if (!en.getValue().isJsonObject()) continue;
            JsonObject o = en.getValue().getAsJsonObject();
            if (o.has("enabled") && !o.get("enabled").getAsBoolean()) off.add(en.getKey());
            if (o.has("rideable")) {
                String v = o.get("rideable").getAsString();
                if (v.equalsIgnoreCase("true")) ride.addProperty(en.getKey(), true);
                else if (v.equalsIgnoreCase("false")) ride.addProperty(en.getKey(), false);
            }
        }
        out.add("disabled", off);
        out.add("rideable", ride);
        return out;
    }

    static void apply(JsonObject modded) {
        Set<String> off = new HashSet<>();
        Map<String, Boolean> ride = new HashMap<>();
        if (modded != null) {
            if (modded.has("disabled") && modded.get("disabled").isJsonArray()) {
                for (JsonElement el : modded.getAsJsonArray("disabled")) off.add(el.getAsString());
            }
            if (modded.has("rideable") && modded.get("rideable").isJsonObject()) {
                for (Map.Entry<String, JsonElement> en : modded.getAsJsonObject("rideable").entrySet()) ride.put(en.getKey(), en.getValue().getAsBoolean());
            }
        }
        disabled = off;
        rideable = ride;
    }
}
