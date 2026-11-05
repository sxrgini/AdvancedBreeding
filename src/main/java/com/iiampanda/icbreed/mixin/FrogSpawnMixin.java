package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.IcBreedConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frog.class)
public abstract class FrogSpawnMixin {
    private static final String[] ICBREED_JUNGLE_VARIANTS = {"blue_poison_dart", "bumblebee_poison_dart", "strawberry_poison_dart"};

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void icbreed$jungleVariants(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        SpawnGroupData data, net.minecraft.nbt.CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return;
        Frog self = (Frog) (Object) this;
        if (!level.getBiome(self.blockPosition()).is(BiomeTags.IS_JUNGLE)) return;
        double chance = IcBreedConfig.get().jungleFrogVariantChance;
        if (chance <= 0) return;
        int index = (int) (self.getRandom().nextDouble() / chance);
        if (index >= ICBREED_JUNGLE_VARIANTS.length) return;
        ResourceLocation key = new ResourceLocation(IcBreed.MOD_ID, ICBREED_JUNGLE_VARIANTS[index]);
        FrogVariant v = net.minecraft.core.registries.BuiltInRegistries.FROG_VARIANT.get(key);
        if (v != null) self.setVariant(v);
    }
}
