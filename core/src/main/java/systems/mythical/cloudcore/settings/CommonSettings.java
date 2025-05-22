package systems.mythical.cloudcore.settings;

public class CommonSettings {
    public static class StringSetting implements Setting<String> {
        private final String name;
        private final String defaultValue;

        public StringSetting(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String parseValue(String value) {
            return value;
        }

        @Override
        public String serializeValue(String value) {
            return value;
        }
    }

    public static class IntegerSetting implements Setting<Integer> {
        private final String name;
        private final Integer defaultValue;

        public IntegerSetting(String name, Integer defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getDefaultValue() {
            return defaultValue;
        }

        @Override
        public Integer parseValue(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public String serializeValue(Integer value) {
            return value.toString();
        }
    }

    public static class BooleanSetting implements Setting<Boolean> {
        private final String name;
        private final Boolean defaultValue;

        public BooleanSetting(String name, Boolean defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean getDefaultValue() {
            return defaultValue;
        }

        @Override
        public Boolean parseValue(String value) {
            return Boolean.parseBoolean(value);
        }

        @Override
        public String serializeValue(Boolean value) {
            return value.toString();
        }
    }

    public static class DoubleSetting implements Setting<Double> {
        private final String name;
        private final Double defaultValue;

        public DoubleSetting(String name, Double defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Double getDefaultValue() {
            return defaultValue;
        }

        @Override
        public Double parseValue(String value) {
            return Double.parseDouble(value);
        }

        @Override
        public String serializeValue(Double value) {
            return value.toString();
        }
    }
} 