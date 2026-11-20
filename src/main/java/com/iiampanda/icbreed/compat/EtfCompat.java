package com.iiampanda.icbreed.compat;

import com.iiampanda.icbreed.genetics.Sex;
import java.util.Locale;
import java.util.Properties;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.property_reading.properties.RandomProperties.RandomPropertyFactory;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.features.state.ETFEntityRenderState;

/**
 * Entity Texture Features support. Adds two random-entity properties usable in any
 * {@code .properties} file of a resource pack:
 * <pre>
 *   advbreeding_sex.1=male
 *   advbreeding_sex.2=female
 *   advbreeding_tamed.3=true
 * </pre>
 */
final class EtfCompat {
    private EtfCompat() {
    }

    static void register() {
        ETFApi.registerCustomRandomPropertyFactory("advbreeding",
                RandomPropertyFactory.of("advbreeding_sex", "advbreeding.etf.sex", EtfCompat::sexProperty),
                RandomPropertyFactory.of("advbreeding_tamed", "advbreeding.etf.tamed", EtfCompat::tamedProperty, true));
    }

    private static RandomProperty sexProperty(Properties props, Integer index) {
        try {
            String raw = RandomProperty.readPropertiesOrThrow(props, index, "advbreeding_sex");
            return new SexProperty(raw.toLowerCase(Locale.ROOT).split("\\s+"));
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }

    private static RandomProperty tamedProperty(Properties props, Integer index) {
        try {
            String raw = RandomProperty.readPropertiesOrThrow(props, index, "advbreeding_tamed");
            return new TamedProperty(Boolean.parseBoolean(raw.trim()));
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }

    private static final class SexProperty extends RandomProperty {
        private final String[] wanted;

        SexProperty(String[] wanted) {
            this.wanted = wanted;
        }

        @Override
        protected boolean testEntityInternal(ETFEntityRenderState state) {
            Sex sex = AnimalInfo.sex(state.entity());
            if (sex == null) return false;
            for (String w : wanted) {
                if (w.equals(sex.id)) return true;
            }
            return false;
        }

        @Override
        public String[] getPropertyIds() {
            return new String[]{"advbreeding_sex"};
        }

        @Override
        protected String getPrintableRuleInfo() {
            return String.join(" ", wanted);
        }
    }

    private static final class TamedProperty extends RandomProperty {
        private final boolean wanted;

        TamedProperty(boolean wanted) {
            this.wanted = wanted;
        }

        @Override
        protected boolean testEntityInternal(ETFEntityRenderState state) {
            return AnimalInfo.tamed(state.entity()) == wanted;
        }

        @Override
        public String[] getPropertyIds() {
            return new String[]{"advbreeding_tamed"};
        }

        @Override
        protected String getPrintableRuleInfo() {
            return String.valueOf(wanted);
        }
    }
}
