package de.flammenfuchs.datacollections.impl.mysql;

import de.flammenfuchs.datacollections.DataCollection;
import de.flammenfuchs.datacollections.DataCollectionException;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public abstract class MysqlDataCollection<T> implements DataCollection<T> {

    private final String tableName;
    private final MySqlFieldBuilder fieldBuilder;
    private final DataSource dataSource;
    private String identifierColumnName;

    @SneakyThrows
    public MysqlDataCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource) {
        this.tableName = tableName;
        this.fieldBuilder = fieldBuilder;
        this.dataSource = dataSource;

        initialize();
    }

    private void initialize() throws DataCollectionException {
        StringJoiner query = new StringJoiner(", ",
                "CREATE TABLE IF NOT EXISTS " + tableName + "(", ")");
        for (FieldEntry fieldEntry : fieldBuilder.getFieldEntries()) {
            String currentField = fieldEntry.getFieldName() + " " + fieldEntry.getFieldType();
            //process properties
            for (FieldProperty property : fieldEntry.getFieldProperties()) {
                if (property == FieldProperty.PRIMARY_KEY) {
                    if (identifierColumnName != null) {
                        throw new DataCollectionException("Multiple primary key fields are not supported!");
                    } else {
                        identifierColumnName = fieldEntry.getFieldName();
                    }
                }
                if (property == FieldProperty.AUTO_INCREMENT) {
                    currentField += " AUTO_INCREMENT";
                } else if (fieldEntry.getDefaultValue() != null) {
                    if (!(fieldEntry.getDefaultValue() instanceof Number)) {

                        currentField += " DEFAULT ('" + fieldEntry.getDefaultValue().toString() + "')";
                    } else {
                        currentField += " DEFAULT (" + fieldEntry.getDefaultValue().toString() + ")";
                    }
                }
            }
            //add field
            query.add(currentField);
        }
        if (identifierColumnName == null) {
            throw new DataCollectionException("Primary Key is missing!");
        }
        query.add("PRIMARY KEY (" + identifierColumnName + ")");

        try (Connection connection = this.dataSource.getConnection()) {
            connection.prepareStatement(query.toString()).executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Type getType(String fieldName) {
        if (isBlockingAllowedInThread()) {
            try (Connection connection = this.dataSource.getConnection()) {
                ResultSet resultSet = connection.prepareStatement(
                        "SELECT " + fieldName + " FROM " + tableName + " LIMIT 1"
                ).executeQuery();
                return Class.forName(resultSet.getMetaData().getColumnClassName(1));
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
                return Void.TYPE;
            }
        } else {
            throw new IllegalStateException("Blocking actions are not allowed in this thread. (threadId=" +
                    Thread.currentThread().getId() + ")");
        }
    }

    @Override
    public <F> F getField(T identifier, String fieldName) {
        if (isBlockingAllowedInThread()) {
            try (Connection connection = this.dataSource.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT ? FROM ? WHERE ? IS ?"
                );
                statement.setString(1, fieldName);
                statement.setString(2, tableName);
                statement.setString(3, identifierColumnName);
                statement.setObject(4, identifier);

                ResultSet resultSet = statement.executeQuery();
                return (F) resultSet.getObject(0);
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            throw new IllegalStateException("Blocking actions are not allowed in this thread. (threadId=" +
                    Thread.currentThread().getId() + ")");
        }
    }

    @Override
    public <F> boolean setField(T identifier, String fieldName, F value) {
        if (isBlockingAllowedInThread()) {
            try (Connection connection = this.dataSource.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO ? (?, ?) VALUES (?, ?) ON DUPLICATE KEY UPDATE ? = ?"
                );
                statement.setString(1, tableName);
                statement.setString(2, identifierColumnName);
                statement.setString(3, fieldName);
                statement.setObject(4, identifier);
                statement.setObject(5, value);
                statement.setString(6, fieldName);
                statement.setObject(7, value);

                return statement.executeUpdate() == 1;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        } else {
            throw new IllegalStateException("Blocking actions are not allowed in this thread. (threadId=" +
                    Thread.currentThread().getId() + ")");
        }
    }

    @Override
    public boolean containsField(T identifier, String fieldName) {
        return getField(identifier, fieldName) != null;
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
}