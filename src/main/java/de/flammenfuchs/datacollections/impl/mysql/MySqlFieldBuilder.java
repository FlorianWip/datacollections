package de.flammenfuchs.datacollections.impl.mysql;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MySqlFieldBuilder {

    @Getter
    private List<FieldEntry> fieldEntries = new ArrayList<>();

    public MySqlFieldBuilder addField(FieldEntry fieldEntry) {
        if (!fieldEntries.contains(fieldEntry)) {
            fieldEntries.add(fieldEntry);
        }
        return this;
    }


    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static class FieldEntry {
        private String fieldName, fieldType;
        private Object defaultValue;
        private FieldProperty[] fieldProperties;

        public static FieldEntry of(String fieldName, String fieldType, FieldProperty... fieldProperties) {
            return new FieldEntry(fieldName, fieldType, null, fieldProperties);
        }

        public static FieldEntry of(String fieldName, String fieldType, Object defaultValue, FieldProperty... fieldProperties) {
            return new FieldEntry(fieldName, fieldType, defaultValue, fieldProperties);
        }
    }

    public enum FieldProperty {
        PRIMARY_KEY,
        AUTO_INCREMENT,
    }

}
