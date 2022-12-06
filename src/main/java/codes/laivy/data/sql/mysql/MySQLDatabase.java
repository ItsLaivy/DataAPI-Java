package codes.laivy.data.sql.mysql;

import codes.laivy.data.sql.SQLDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MySQLDatabase extends SQLDatabase {

    public MySQLDatabase(@NotNull MySQLDatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    @Override
    public @NotNull MySQLDatabaseType getDatabaseType() {
        return (MySQLDatabaseType) super.getDatabaseType();
    }

    @Override
    protected @NotNull MySQLStatement statement(String query) {
        new MySQLStatement(getDatabaseType(), "USE " + getName() + ";").execute();
        return new MySQLStatement(getDatabaseType(), query);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MySQLDatabase)) return false;
        MySQLDatabase database = (MySQLDatabase) o;
        return getDatabaseType().equals(database.getDatabaseType()) && getName().equals(database.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabaseType(), getName());
    }

}
