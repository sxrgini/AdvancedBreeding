package com.iiampanda.icbreed;

import com.iiampanda.icbreed.platform.AttachmentRegistry;
import com.iiampanda.icbreed.platform.DropsModifier;
import com.iiampanda.icbreed.platform.Net;
import com.iiampanda.icbreed.platform.NeoHooks;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(IcBreed.MOD_ID)
public final class IcBreedNeo {
    public IcBreedNeo(IEventBus modBus) {
        NeoHooks.init();
        new IcBreed().onInitialize();
        AttachmentRegistry.bind(modBus);
        modBus.addListener(Net::onRegister);
        modBus.addListener(IcBreedNeo::onRegister);
        DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier>> glm =
                DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IcBreed.MOD_ID);
        glm.register("drops", () -> DropsModifier.CODEC);
        glm.register(modBus);
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) com.iiampanda.icbreed.block.ModBlocks.init();
    }
}
