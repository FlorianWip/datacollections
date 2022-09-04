package de.flammenfuchs.datacollections;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

public interface DataCollection<T> {

    Type getType(String fieldName);

    boolean isBlockingAllowedInThread();

    <F> F getField(T identifier, String fieldName);

    <F> boolean setField(T identifier, String fieldName, F value);

    boolean containsField(T identifier, String fieldName);

    CompletableFuture<Type> getTypeAsync(String fieldName);

    <F> CompletableFuture<F> getFieldAsync(T identifier, String fieldName);

    <F> CompletableFuture<Boolean> setFieldAsync(T identifier, String fieldName, F value);

    CompletableFuture<Boolean> containsFieldAsync(T identifier, String fieldName);

}
