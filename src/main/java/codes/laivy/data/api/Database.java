package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.query.DatabaseType;
import codes.laivy.data.sql.SQLTable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

public abstract class Database {

    protected final @NotNull DatabaseType<?, ?> databaseType;
    protected final @NotNull String name;

    public <R extends Receptor, V extends Variable> Database(@NotNull DatabaseType<R, V> databaseType, @NotNull String name) {
        this.databaseType = databaseType;
        this.name = name;

        if (DataAPI.getDatabase(databaseType, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A database named '" + name + "' of type '" + databaseType.getName() + "' already exists!");
            return;
        }

        getDatabaseType().databaseLoad(this);

        DataAPI.DATABASES.get(databaseType).add(this);
        DataAPI.TABLES.put(this, new HashSet<>());
    }

    public @NotNull Variable[] getVariables() {
        //noinspection ConstantConditions
        return (Variable[]) DataAPI.VARIABLES.get(this).toArray();
    }
    public @NotNull Receptor[] getReceptors() {
        //noinspection ConstantConditions
        return (Receptor[]) DataAPI.RECEPTORS.get(this).toArray();
    }

    @NotNull
    public DatabaseType<?, ?> getDatabaseType() {
        return databaseType;
    }
    @NotNull
    public String getName() {
        return name;
    }

    public void delete() {
        for (SQLTable SQLTable : getTables()) {
            SQLTable.delete();
        }
        getDatabaseType().databaseDelete(this);
    }

    public SQLTable[] getTables() {
        SQLTable[] SQLTables = new SQLTable[DataAPI.TABLES.get(this).size()];
        int row = 0;
        for (SQLTable SQLTable : DataAPI.TABLES.get(this)) {
            SQLTables[row] = SQLTable;
            row++;
        }
        return SQLTables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Database)) return false;
        Database database = (Database) o;
        return getDatabaseType().equals(database.getDatabaseType()) && getName().equals(database.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabaseType(), getName());
    }
}