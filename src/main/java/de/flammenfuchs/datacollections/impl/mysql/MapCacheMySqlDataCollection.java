package de.flammenfuchs.datacollections.impl.mysql;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class MapCacheMySqlDataCollection<T> extends MysqlDataCollection<T> {

    private Map<T, Object> cache = new HashMap<>();

    public MapCacheMySqlDataCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource) {
        super(tableName, fieldBuilder, dataSource);
    }

    @Override
    public <F> F getField(T identifier, String fieldName) {
        if (cache.containsKey(identifier)) {
            return (F) cache.get(identifier);
        }
        F result = super.getField(identifier, fieldName);
        cache.put(identifier, result);
        return result;
    }

    @Override
    public <F> boolean setField(T identifier, String fieldName, F value) {
        boolean result = super.setField(identifier, fieldName, value);
        if (result) {
            cache.put(identifier, value);
        }
        return result;
    }

    @Override
    public boolean containsField(T identifier, String fieldName) {
        return cache.containsKey(identifier) || super.containsField(identifier, fieldName);
    }

    public void removeFromCache(T identifier) {
        this.cache.remove(identifier);
    }
}
