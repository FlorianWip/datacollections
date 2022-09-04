package de.flammenfuchs.datacollections.impl.map;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.flammenfuchs.datacollections.DataCollection;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MapDataCollection<T> implements DataCollection<T> {

    private final File jsonFile;
    private Map<T, Map<String, Object>> container;

    @SneakyThrows
    public MapDataCollection(Class<T> paramClass, String directoryPath, String fileName) {
        File directory = new File(directoryPath);
        jsonFile = new File(directoryPath, fileName);
        directory.mkdirs();
        if (jsonFile.isDirectory()) {
            throw new IllegalArgumentException("Given target file is a directory");
        }
        if (jsonFile.exists()) {
            Reader reader = new FileReader(jsonFile);
            Type type = TypeToken.getParameterized(
                    HashMap.class,
                    paramClass,
                    TypeToken.getParameterized(HashMap.class, String.class, Object.class).getType()
            ).getType();
            container = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .fromJson(reader, type);
            if (container == null) {
                container = new HashMap<>();
            }
        } else {
            container = new HashMap<>();
        }
    }


    @Override
    public Type getType(String fieldName) {
        throw new UnsupportedOperationException("Type is not predefined in this implementation");
    }

    @Override
    public boolean isBlockingAllowedInThread() {
        return true;
    }

    @Override
    public <F> F getField(T identifier, String fieldName) {
        if (!container.containsKey(identifier)) {
            return null;
        } else if (!container.get(identifier).containsKey(fieldName.toLowerCase())) {
            return null;
        }
        return (F) container.get(identifier).get(fieldName.toLowerCase());
    }

    @Override
    public <F> boolean setField(T identifier, String fieldName, F value) {
        if (!container.containsKey(identifier)) {
            container.put(identifier, new HashMap<>());
        }
        container.get(identifier).put(fieldName.toLowerCase(), value);
        return true;
    }

    @Override
    public boolean containsField(T identifier, String fieldName) {
        return container.containsKey(identifier) && container.get(identifier).containsKey(fieldName.toLowerCase());
    }

    @Override
    public CompletableFuture<Type> getTypeAsync(String fieldName) {
        return CompletableFuture.supplyAsync(() -> getType(fieldName));
    }

    @Override
    public <F> CompletableFuture<F> getFieldAsync(T identifier, String fieldName) {
        return CompletableFuture.supplyAsync(() -> getField(identifier, fieldName));
    }

    @Override
    public <F> CompletableFuture<Boolean> setFieldAsync(T identifier, String fieldName, F value) {
        return CompletableFuture.supplyAsync(() -> setField(identifier, fieldName, value));
    }

    @Override
    public CompletableFuture<Boolean> containsFieldAsync(T identifier, String fieldName) {
        return CompletableFuture.supplyAsync(() -> containsField(identifier, fieldName));
    }

    @SneakyThrows
    public void save() {
        FileWriter writer = new FileWriter(jsonFile);
        new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(container, writer);
        writer.flush();
        writer.close();
    }
}
