package codes.laivy.data.sql;

import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

public abstract class SQLDatabaseType extends DatabaseType<SQLReceptor, SQLVariable> {
    public SQLDatabaseType(@NotNull String name) {
        super(name);
    }

    public abstract void tableLoad(@NotNull SQLTable SQLTable);
    public abstract void tableDelete(@NotNull SQLTable SQLTable);
}
