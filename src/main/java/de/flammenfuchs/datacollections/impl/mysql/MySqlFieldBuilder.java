package de.flammenfuchs.datacollections.impl.mysql;

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


}
