package com.iiampanda.icbreed;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

public final class IcBreedConfig {
    private static final int CURRENT_VERSION = 6;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int configVersion;

    public double statIncreaseChance = 0.25;

    public double worseChanceOnHit = 0.33;

    public int gestationSeconds = 600;

    public double youngSpawnChance = 0.15;

    public double naturalSpawnKeep = 0.5;

    public int tamingFoodSeconds = 300;

    public int starveSeconds = 3600;

    public int starveIntervalSeconds = 30;

    public double feedHealAmount = 4.0;

    public int bedRadius = 10;

    public int penMaxTiles = 256;

    public double nameTagLootChance = 0.0001;

    public double auraRange = 50;
    public int milkCooldownSeconds = 300;
    public int postBirthCooldownSeconds = 300;

    public double baseMutationChance = 0.0;

    public int maxGenotypePoints = 60;

    public boolean showSexSymbol = true;

    private static IcBreedConfig instance = new IcBreedConfig();

    public static IcBreedConfig get() {
        return instance;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("advbreeding.json");
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                IcBreedConfig loaded = GSON.fromJson(r, IcBreedConfig.class);

                if (loaded != null && loaded.configVersion >= CURRENT_VERSION) instance = loaded;
            } catch (Exception e) {
                IcBreed.LOGGER.warn("Could not read advbreeding.json, using defaults", e);
            }
        }
        instance.configVersion = CURRENT_VERSION;
        instance.sanitize();
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            IcBreed.LOGGER.warn("Could not write advbreeding.json", e);
        }
    }

    private void sanitize() {
        statIncreaseChance = clamp01(statIncreaseChance);
        worseChanceOnHit = clamp01(worseChanceOnHit);
        baseMutationChance = clamp01(baseMutationChance);
        gestationSeconds = Math.max(1, gestationSeconds);
        starveSeconds = Math.max(1, starveSeconds);
        starveIntervalSeconds = Math.max(1, starveIntervalSeconds);
        feedHealAmount = Math.max(0, feedHealAmount);
        postBirthCooldownSeconds = Math.max(0, postBirthCooldownSeconds);
        maxGenotypePoints = Math.max(0, Math.min(70, maxGenotypePoints));
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
