package codes.laivy.data.sql.sqlite;

import codes.laivy.data.sql.SQLDatabase;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.util.Objects;

public class SQLiteDatabase extends SQLDatabase {

    private @Nullable Connection connection;

    public SQLiteDatabase(@NotNull SQLiteDatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);

        getDatabaseType().databaseLoad(this);
    }

    @Override
    public @NotNull SQLiteDatabaseType getDatabaseType() {
        return (SQLiteDatabaseType) super.getDatabaseType();
    }

    @NotNull
    public File getFile() {
        return new File(getDatabaseType().getPath() + File.separator + getName() + ".db");
    }

    public @NotNull Connection getConnection() {
        return Objects.requireNonNull(connection);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLiteDatabase)) return false;
        SQLiteDatabase database = (SQLiteDatabase) o;
        return getDatabaseType().equals(database.getDatabaseType()) && getName().equals(database.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabaseType(), getName());
    }

}
