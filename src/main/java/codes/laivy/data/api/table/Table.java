package codes.laivy.data.api.table;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;

public abstract class Table {

    private final @NotNull String name;
    private final @NotNull Database database;

    public abstract void delete();

    public abstract Variable[] getVariables();
    public abstract Receptor[] getReceptors();

    public Table(@NotNull Database database, @NotNull String name) {
        this.name = name;
        this.database = database;

        DataAPI.TABLES.get(database).add(this);
    }

    public @NotNull String getName() {
        return name;
    }
    public @NotNull Database getDatabase() {
        return database;
    }

}
