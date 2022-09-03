package codes.laivy.data.sql.sqlite;

import codes.laivy.data.sql.SQLDatabase;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;

public class SQLiteDatabase extends SQLDatabase {

    private Connection connection;

    public SQLiteDatabase(@NotNull SQLiteDatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    @NotNull
    public File getFile() {
        return new File(((SQLiteDatabaseType) getDatabaseType()).getPath() + File.separator + getName() + ".db");
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }
    @ApiStatus.Internal
    public void setConnection(@NotNull Connection connection) {
        this.connection = connection;
    }

    @Override
    protected @NotNull SQLiteStatement statement(String query) {
        return new SQLiteStatement(this, query);
    }

    @Override
    public @NotNull SQLiteResult query(String query) {
        SQLiteStatement statement = statement(query);
        return statement.execute();
    }

    @Override
    protected void open() {

    }

    @Override
    protected void close() {

    }
}
