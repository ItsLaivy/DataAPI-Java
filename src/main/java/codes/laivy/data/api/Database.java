package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
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
        DataAPI.TABLES.put(this, new LinkedHashSet<>());
        DataAPI.RECEPTORS.put(this, new LinkedHashSet<>());
    }

    public @NotNull Table[] getTables() {
        return DataAPI.TABLES.get(this).toArray(new Table[0]);
    }
    public @NotNull Variable[] getVariables() {
        return new LinkedHashSet<>(DataAPI.VARIABLES.get(this)).toArray(new Variable[0]);
    }
    public @NotNull Receptor[] getReceptors() {
        return new LinkedHashSet<>(DataAPI.RECEPTORS.get(this)).toArray(new Receptor[0]);
    }

    /**
     * @return A Set containing all receptors (unloaded and loaded) from this database
     */
    public @NotNull Receptor[] getAllReceptors() {
        return getDatabaseType().receptorList();
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
        for (Table table : getTables()) {
            table.delete();
        }
        getDatabaseType().databaseDelete(this);
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