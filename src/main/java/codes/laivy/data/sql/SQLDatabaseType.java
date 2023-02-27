package codes.laivy.data.sql;

import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class SQLDatabaseType extends DatabaseType<SQLReceptor, SQLVariable> {
    public SQLDatabaseType(@NotNull String name) {
        super(name);
    }

    public abstract void tableLoad(@NotNull SQLTable SQLTable);
    public abstract void tableDelete(@NotNull SQLTable SQLTable);

    public abstract @NotNull SQLDatabase[] getDatabases();
    public @NotNull SQLTable[] getTables() {
        Set<SQLTable> tables = new LinkedHashSet<>();
        for (SQLDatabase database : getDatabases()) {
            tables.addAll(Arrays.asList(database.getTables()));
        }
        return tables.toArray(new SQLTable[0]);
    }
}
