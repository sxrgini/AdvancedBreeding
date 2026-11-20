package com.iiampanda.icbreed.compat;

import com.iiampanda.icbreed.genetics.Sex;
import java.util.function.BooleanSupplier;
import traben.entity_model_features.EMFAnimationApi;

/**
 * Entity Model Features support. Adds boolean animation variables usable in
 * {@code .jem} / {@code .jpm} animation expressions:
 * <pre>
 *   advbreeding_male, advbreeding_female, advbreeding_tamed
 * </pre>
 */
final class EmfCompat {
    private EmfCompat() {
    }

    static void register() throws Exception {
        bool("advbreeding_male", () -> AnimalInfo.sex(EMFAnimationApi.getCurrentEntity()) == Sex.MALE);
        bool("advbreeding_female", () -> AnimalInfo.sex(EMFAnimationApi.getCurrentEntity()) == Sex.FEMALE);
        bool("advbreeding_tamed", () -> AnimalInfo.tamed(EMFAnimationApi.getCurrentEntity()));
    }

    private static void bool(String name, BooleanSupplier supplier) throws Exception {
        EMFAnimationApi.registerSingletonAnimationVariable("advbreeding", name, "advbreeding.emf." + name.substring("advbreeding_".length()), supplier);
    }
}
