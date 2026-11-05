package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.IcBreedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frog.class)
public abstract class FrogSpawnMixin {
    private static final String[] ICBREED_JUNGLE_VARIANTS = {"blue_poison_dart", "bumblebee_poison_dart", "strawberry_poison_dart"};

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void icbreed$jungleVariants(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return;
        Frog self = (Frog) (Object) this;
        if (!level.getBiome(self.blockPosition()).is(BiomeTags.IS_JUNGLE)) return;
        double chance = IcBreedConfig.get().jungleFrogVariantChance;
        if (chance <= 0) return;
        int index = (int) (self.getRandom().nextDouble() / chance);
        if (index >= ICBREED_JUNGLE_VARIANTS.length) return;
        ResourceKey<FrogVariant> key = ResourceKey.create(Registries.FROG_VARIANT, Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, ICBREED_JUNGLE_VARIANTS[index]));
        level.registryAccess().lookupOrThrow(Registries.FROG_VARIANT).get(key).ifPresent(h -> ((PhenoFrogAccessor) (Object) self).icbreed$set(h));
    }
}
