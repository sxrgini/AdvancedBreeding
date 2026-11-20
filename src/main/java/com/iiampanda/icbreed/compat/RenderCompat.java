package com.iiampanda.icbreed.compat;

import com.iiampanda.icbreed.IcBreed;

/** Optional integration with Entity Texture Features and Entity Model Features. Does nothing when they are absent. */
public final class RenderCompat {
    private RenderCompat() {
    }

    private static boolean loaded(String id) {
        return net.minecraftforge.fml.ModList.get().isLoaded(id);
    }

    /** Call from the client initializer. */
    public static void init() {
        if (loaded("entity_texture_features")) {
            try {
                EtfCompat.register();
                IcBreed.LOGGER.info("Advanced Breeding: Entity Texture Features support enabled (advbreeding_sex, advbreeding_tamed)");
            } catch (Throwable t) {
                IcBreed.LOGGER.warn("Advanced Breeding: could not register Entity Texture Features support", t);
            }
        }
        if (loaded("entity_model_features")) {
            try {
                EmfCompat.register();
                IcBreed.LOGGER.info("Advanced Breeding: Entity Model Features support enabled (advbreeding_male, advbreeding_female, advbreeding_tamed)");
            } catch (Throwable t) {
                IcBreed.LOGGER.warn("Advanced Breeding: could not register Entity Model Features support", t);
            }
        }
    }
}
