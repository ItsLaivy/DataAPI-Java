package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class Database {

    private final DatabaseType databaseType;
    private final String name;

    public Database(@NotNull DatabaseType databaseType, @NotNull String name) {
        this.databaseType = databaseType;
        this.name = name;

        if (DataAPI.getDatabase(databaseType, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A database named '" + name + "' of type '" + databaseType.getName() + "' already exists!");
            return;
        }

        DataAPI.DATABASE_QUERIES.put(this, 0);
        databaseType.databaseLoad(this);

        DataAPI.DATABASES.get(databaseType).add(this);
        DataAPI.TABLES.put(this, new ArrayList<>());
    }

    @NotNull
    public DatabaseType getDatabaseType() {
        return databaseType;
    }
    @NotNull
    public String getName() {
        return name;
    }

    public void delete() {
        for (Table table : getTables()) {
            table.delete();
        }
        getDatabaseType().databaseDelete(this);
    }

    public Table[] getTables() {
        Table[] tables = new Table[DataAPI.TABLES.get(this).size()];
        int row = 0;
        for (Table table : DataAPI.TABLES.get(this)) {
            tables[row] = table;
            row++;
        }
        return tables;
    }

}