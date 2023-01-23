package codes.laivy.data.api;

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
    }

    public @NotNull String getName() {
        return name;
    }
    public @NotNull Database getDatabase() {
        return database;
    }

}
