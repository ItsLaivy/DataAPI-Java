package codes.laivy.data.sql.mysql;

import codes.laivy.data.query.DatabaseType;
import codes.laivy.data.sql.SQLDatabase;
import org.jetbrains.annotations.NotNull;

public class MySQLDatabase extends SQLDatabase {

    public MySQLDatabase(@NotNull DatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    @Override
    protected @NotNull MySQLStatement statement(String query) {
        return new MySQLStatement(this, query);
    }

    @Override
    public @NotNull MySQLResult query(String query) {
        MySQLStatement statement = statement(query);
        return statement.execute();
    }

    @Override
    protected void open() {

    }

    @Override
    protected void close() {

    }

}
