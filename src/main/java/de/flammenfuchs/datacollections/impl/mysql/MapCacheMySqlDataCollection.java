package de.flammenfuchs.datacollections.impl.mysql;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class MapCacheMySqlDataCollection<T> extends MysqlDataCollection<T> {

    private Map<T, Map<String, Object>> container = new HashMap<>();

    public MapCacheMySqlDataCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource) {
        super(tableName, fieldBuilder, dataSource);
    }

    @Override
    public <F> F getField(T identifier, String fieldName) {
        if (container.containsKey(identifier) && container.get(identifier).containsKey(fieldName.toLowerCase())) {
            return (F) container.get(identifier).get(fieldName.toLowerCase());
        } else {
            container.put(identifier, new HashMap<>());
        }
        F result = super.getField(identifier, fieldName);
        container.get(identifier).put(fieldName.toLowerCase(), result);
        return result;
    }

    @Override
    public <F> boolean setField(T identifier, String fieldName, F value) {
        boolean result = super.setField(identifier, fieldName, value);
        if (result) {
            if (!container.containsKey(identifier)) {
                container.put(identifier, new HashMap<>());
            }
            container.get(identifier).put(fieldName.toLowerCase(), value);
        }
        return result;
    }

    @Override
    public boolean containsField(T identifier, String fieldName) {
        return (container.containsKey(identifier) && container.get(identifier).containsKey(fieldName.toLowerCase()))
                || super.containsField(identifier, fieldName);
    }

    public void removeFromCache(T identifier) {
        this.container.remove(identifier);
    }
}
