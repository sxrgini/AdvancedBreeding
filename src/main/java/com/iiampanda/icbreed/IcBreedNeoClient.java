package com.iiampanda.icbreed;

import com.iiampanda.icbreed.client.IcBreedClient;
import com.iiampanda.icbreed.platform.NeoClientHooks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = IcBreed.MOD_ID, dist = Dist.CLIENT)
public final class IcBreedNeoClient {
    public IcBreedNeoClient(IEventBus modBus) {
        NeoClientHooks.init(modBus);
        new IcBreedClient().onInitializeClient();
    }
}
