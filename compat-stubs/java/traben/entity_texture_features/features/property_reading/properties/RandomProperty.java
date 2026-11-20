package traben.entity_texture_features.features.property_reading.properties;

import java.util.Properties;
import traben.entity_texture_features.features.state.ETFEntityRenderState;

public abstract class RandomProperty {
    public static class RandomPropertyException extends Exception {
        public RandomPropertyException(String message) {
            super(message);
        }
    }

    public static String readPropertiesOrThrow(Properties properties, int propertyNum, String... propertyIds) throws RandomPropertyException {
        throw new UnsupportedOperationException("stub");
    }

    protected abstract boolean testEntityInternal(ETFEntityRenderState state);

    public abstract String[] getPropertyIds();

    protected abstract String getPrintableRuleInfo();
}
