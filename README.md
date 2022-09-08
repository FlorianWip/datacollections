# Library: datacollections
This is a library for cross technology saving of datacollections.
Its aim is to allow to exchange the saving technology without
modifying code, which accesses the datacollection.

## Features
- Premade implementation for MySQL
- Premade implementation for saving as json
- Easy to implement

## Maven
### Repository
````
<repository>
  <id>flammenfuchs-repo-public</id>
  <name>Flammenfuchs_YT's Repository</name>
  <url>https://repo.flammenfuchs.de/public</url>
</repository>
````
### Dependency
````
<dependency>
    <groupId>de.flammenfuchs</groupId>
    <artifactId>datacollections</artifactId>
    <version>1.0.0</version>
</dependency>
````
## How to use?
Example implementation for MySQL and for JSON
### MySQL
Create a testcollection
````
public class TestCollection extends MysqlDataCollection<String> {
    public TestCollection(String tableName, MySqlFieldBuilder fieldBuilder, DataSource dataSource) {
        super(tableName, fieldBuilder, dataSource);
    }

    @Override
    public boolean isBlockingAllowedInThread() {
        return true; //allows blocking actions in every thread
    }
}
````
Guide for the required constructor parameters:
1. String tableName: The name of the MySQL Table, which will be created
2. MySQLFieldBuilder: Defines the column (fields) of the table. The order of the list is the order of the table
````
public class TestCollection extends MysqlDataCollection<String> {
    public TestCollection(String tableName, DataSource dataSource) {
        super(tableName,
                new MySqlFieldBuilder() //add some example fields
                        .addField(FieldEntry.of("sku" //column name
                                , "tinytext" //mysql datatype
                                , "0-00" //(optional) default value (used when insert in mysql)
                                , FieldProperty.PRIMARY_KEY)) //(FieldProperty... can be with length of 0 if not needed
                        .addField(FieldEntry.of("price", "double", 0.00)) //some other column
                , dataSource);
    }

    @Override
    public boolean isBlockingAllowedInThread() {
        return true; //allows blocking actions in every thread
    }
}
````
3. javax.sql.DataSource

### JSON
Create a testcollection
````
public class TestCollection extends MapDataCollection<String> {

    public TestCollection(String directoryPath, String fileName) {
        super(String.class, directoryPath, fileName);
    }

    @Override
    public boolean isBlockingAllowedInThread() {
        return true; //allows blocking actions in every thread
    }
}
````
Guide for the required constructor parameters:
1. Class of identifier type (in this example String)
2. path (relative or absolut) to the directory
3. filename

Important:
Don't forget to save the MapDataCollection before exiting the application to prevent 
data loss. Saving while running the application is allowed and has no consequences for existing data

