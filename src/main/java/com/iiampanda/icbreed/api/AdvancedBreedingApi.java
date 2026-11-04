package com.iiampanda.icbreed.api;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class AdvancedBreedingApi {
    public static final TagKey<EntityType<?>> RIDEABLE_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("advbreeding", "rideable"));
    public static final TagKey<EntityType<?>> NOT_RIDEABLE_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("advbreeding", "not_rideable"));

    private static final Set<EntityType<?>> RIDEABLE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private AdvancedBreedingApi() {
    }

    public static void registerRideable(EntityType<?> type) {
        RIDEABLE.add(type);
    }

    public static boolean isRideableType(Mob mob) {
        EntityType<?> type = mob.getType();
        Boolean forced = com.iiampanda.icbreed.config.ModdedMobs.rideableOverride(type);
        if (forced != null) return forced;
        if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(NOT_RIDEABLE_TAG)) return false;
        if (RIDEABLE.contains(type) || net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(RIDEABLE_TAG)) return true;
        return mob instanceof PlayerRideable
                || mob instanceof PlayerRideableJumping
                || mob instanceof ItemSteerable
                || mob instanceof AbstractHorse;
    }
}
