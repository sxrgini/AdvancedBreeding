package com.iiampanda.icbreed;

import com.iiampanda.icbreed.platform.AttSync;
import com.iiampanda.icbreed.platform.ForgeHooks;
import com.iiampanda.icbreed.platform.Net;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IcBreed.MOD_ID)
public final class IcBreedForge {
    public IcBreedForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(com.iiampanda.icbreed.block.ModBlocks::register);
        ForgeHooks.init();
        AttSync.init();
        new IcBreed().onInitialize();
        Net.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> IcBreedForgeClient.init(FMLJavaModLoadingContext.get().getModEventBus()));
    }
}
