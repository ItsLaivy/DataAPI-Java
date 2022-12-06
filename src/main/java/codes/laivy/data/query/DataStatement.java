package codes.laivy.data.query;

import org.jetbrains.annotations.NotNull;

public abstract class DataStatement {

    private final String query;

    public DataStatement(@NotNull String query) {
        this.query = query;
    }

    public @NotNull String getQuery() {
        return query;
    }
    public @NotNull abstract DataResult execute();

    public abstract void close();

}
