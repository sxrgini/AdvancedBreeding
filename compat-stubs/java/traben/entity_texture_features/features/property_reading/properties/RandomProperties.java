package traben.entity_texture_features.features.property_reading.properties;

import java.util.Properties;
import java.util.function.BiFunction;

public final class RandomProperties {
    public interface RandomPropertyFactory {
        static RandomPropertyFactory of(String propertyId, String explanationTranslationKey, BiFunction<Properties, Integer, RandomProperty> factory) {
            throw new UnsupportedOperationException("stub");
        }

        static RandomPropertyFactory of(String propertyId, String explanationTranslationKey, BiFunction<Properties, Integer, RandomProperty> factory, boolean updatesOverTime) {
            throw new UnsupportedOperationException("stub");
        }
    }
}
