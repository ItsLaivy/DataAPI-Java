package codes.laivy.data.sql;

import codes.laivy.data.api.Database;
import codes.laivy.data.query.DataResult;
import codes.laivy.data.query.DataStatement;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

public abstract class SQLDatabase extends Database {

    public SQLDatabase(@NotNull DatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    protected abstract @NotNull DataStatement statement(String query);
    public abstract @NotNull DataResult query(String query);

    protected abstract void open();
    protected abstract void close();

}
