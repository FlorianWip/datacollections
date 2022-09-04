package de.flammenfuchs.datacollections.impl.mysql;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

public abstract class AutoCachedMySqlDataCollection<T> extends MysqlDataCollection<T> {

    private final long timeToLive;
    private final TimeUnit timeUnit;
    private final Cache<T, Object> cache;

    public AutoCachedMySqlDataCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource,
                                         long ttl, TimeUnit timeUnit) {
        super(tableName, fieldBuilder, dataSource);
        this.timeUnit = timeUnit;
        this.timeToLive = ttl;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(timeToLive, timeUnit)
                .expireAfterWrite(timeToLive, timeUnit)
                .build();
    }

    public AutoCachedMySqlDataCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource,
                                         long ttlMillis) {
        super(tableName, fieldBuilder, dataSource);
        this.timeToLive = ttlMillis;
        this.timeUnit = TimeUnit.MILLISECONDS;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(timeToLive, timeUnit)
                .expireAfterWrite(timeToLive, timeUnit)
                .build();
    }

    @Override
    public <F> F getField(T identifier, String fieldName) {
        F cached = (F) cache.getIfPresent(identifier);
        if (cached == null) {
            cached = super.getField(identifier, fieldName);
            cache.put(identifier, cached);
        }
        return cached;
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
        return cache.getIfPresent(identifier) != null || super.containsField(identifier, fieldName);
    }
}
