package codes.laivy.data.query;

import codes.laivy.data.api.Database;
import org.jetbrains.annotations.NotNull;

public abstract class DataStatement {

    private final Database database;
    private final String query;

    public DataStatement(@NotNull Database database, @NotNull String query) {
        this.database = database;
        this.query = query;
    }

    public @NotNull Database getDatabase() {
        return database;
    }
    public @NotNull String getQuery() {
        return query;
    }
    public @NotNull abstract DataResult execute();

    public abstract void close();

}
