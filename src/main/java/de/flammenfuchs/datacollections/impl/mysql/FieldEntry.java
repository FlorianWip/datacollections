package de.flammenfuchs.datacollections.impl.mysql;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class FieldEntry {
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
