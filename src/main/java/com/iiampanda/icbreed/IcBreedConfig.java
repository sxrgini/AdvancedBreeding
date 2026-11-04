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
    private static final int CURRENT_VERSION = 7;
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

    public double phenotypeMutationChance = 0.10;
    public double grownYoungChance = 0.01;
    public int parentRange = 16;
    public double catAuraRange = 16.0;
    public int nineLivesCooldownSeconds = 3600;
    public int honeyDripSeconds = 45;
    public int dolphinLoveSeconds = 30;
    public int dolphinBreedCooldownSeconds = 300;
    public int milkReductionSecondsPerRank = 60;
    public int milkMinimumSeconds = 120;
    public int llamaWoolCooldownSeconds = 300;
    public double productionChancePerRank = 0.11;
    public int ghastDeckRadius = 8;
    public int ghastDeckMinY = 4;
    public int ghastDeckMaxY = 24;
    public int ghastDeckMaxBlocks = 600;
    public int ghastDeckMaxSeats = 4;
    public int ghastDeckSettleTicks = 30;

    private static IcBreedConfig instance = new IcBreedConfig();

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static IcBreedConfig get() {
        return instance;
    }

    public static void load() {
        Path path = configDir().resolve("advbreeding.json");
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                IcBreedConfig loaded = GSON.fromJson(r, IcBreedConfig.class);

                if (loaded != null) instance = loaded;
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
        phenotypeMutationChance = clamp01(phenotypeMutationChance);
        grownYoungChance = clamp01(grownYoungChance);
        productionChancePerRank = clamp01(productionChancePerRank);
        parentRange = Math.max(1, parentRange);
        catAuraRange = Math.max(1, catAuraRange);
        nineLivesCooldownSeconds = Math.max(0, nineLivesCooldownSeconds);
        honeyDripSeconds = Math.max(1, honeyDripSeconds);
        dolphinLoveSeconds = Math.max(1, dolphinLoveSeconds);
        dolphinBreedCooldownSeconds = Math.max(0, dolphinBreedCooldownSeconds);
        milkReductionSecondsPerRank = Math.max(0, milkReductionSecondsPerRank);
        milkMinimumSeconds = Math.max(1, milkMinimumSeconds);
        llamaWoolCooldownSeconds = Math.max(0, llamaWoolCooldownSeconds);
        ghastDeckRadius = Math.max(1, ghastDeckRadius);
        ghastDeckMinY = Math.max(0, ghastDeckMinY);
        ghastDeckMaxY = Math.max(ghastDeckMinY + 1, ghastDeckMaxY);
        ghastDeckMaxBlocks = Math.max(1, ghastDeckMaxBlocks);
        ghastDeckMaxSeats = Math.max(0, ghastDeckMaxSeats);
        ghastDeckSettleTicks = Math.max(1, ghastDeckSettleTicks);
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
