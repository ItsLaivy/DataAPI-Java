package codes.laivy.data.sql.sqlite;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Table;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.DriverManager;
import java.util.Map;

public class SQLiteDatabaseType extends DatabaseType {

    public final File path;

    public SQLiteDatabaseType(@NotNull String path) {
        this(new File(path));
    }
    public SQLiteDatabaseType(@NotNull File path) {
        super("SQLITE");
        this.path = path;
    }

    @NotNull
    public File getPath() {
        return path;
    }

    private void query(@NotNull SQLiteDatabase database, @NotNull String query) {
        try {
            database.query(query);
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public @NotNull String[] suppressedErrors() {
        return new String[] {
                "SQL error or missing database (table",
                "SQL error or missing database (duplicate column name:",
        };
    }

    @Override
    public @NotNull Map<String, String> data(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an SQLite database!");
        }

        return ((SQLiteDatabase) receptor.getTable().getDatabase()).query("SELECT * FROM '" + receptor.getTable().getName() + "' WHERE bruteid = '" + receptor.getBruteId() + "'").results();
    }

    @Override
    public void receptorLoad(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an SQLite database!");
        }

        Map<String, String> data = data(receptor);
        if (data.isEmpty()) {
            query((SQLiteDatabase) receptor.getTable().getDatabase(), "INSERT INTO '" + receptor.getTable().getName() + "' (name,bruteid,last_update) VALUES ('" + receptor.getName() + "','" + receptor.getBruteId() + "','" + DataAPI.getDate() + "')");
            data = data(receptor);
        }

        int row = 0;
        for (Map.Entry<String, String> map : data.entrySet()) {
            if (row > 3) {
                new InactiveVariable(receptor, map.getKey(), map.getValue());
            }
            row++;
        }
    }

    @Override
    public void receptorDelete(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an SQLite database!");
        }

        query((SQLiteDatabase) receptor.getTable().getDatabase(), "DELETE FROM '" + receptor.getTable().getName() + "' WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void save(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an SQLite database!");
        }

        StringBuilder query = new StringBuilder();
        for (ActiveVariable variable : receptor.getActiveVariables()) {
            query.append("`").append(variable.getVariable().getName()).append("`='").append(Variable.serialize(variable.getValue())).append("',");
        }
        query.append("`last_update`='").append(DataAPI.getDate()).append("'");

        query((SQLiteDatabase) receptor.getTable().getDatabase(), "UPDATE '" + receptor.getTable().getName() + "' SET " + query + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void tableLoad(@NotNull Table table) {
        if (!(table.getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This table's database isn't an SQLite database!");
        }

        try {
            query((SQLiteDatabase) table.getDatabase(), "CREATE TABLE '" + table.getName() + "' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, bruteid TEXT, 'last_update' TEXT);");
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void tableDelete(@NotNull Table table) {
        if (!(table.getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This table's database isn't an SQLite database!");
        }

        query((SQLiteDatabase) table.getDatabase(), "DROP TABLE '" + table.getName() + "'");
    }

    @Override
    public void variableLoad(@NotNull Variable variable) {
        if (!(variable.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This variable's database isn't an SQLite database!");
        }

        try {
            query((SQLiteDatabase) variable.getTable().getDatabase(), "ALTER TABLE '" + variable.getTable().getName() + "' ADD COLUMN '" + variable.getName() + "' TEXT DEFAULT '" + Variable.serialize(variable.getDefaultValue()) + "';");
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void variableDelete(@NotNull Variable variable) {
        if (!(variable.getTable().getDatabase().getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This variable's database isn't an SQLite database!");
        }

        //throw new IllegalStateException("The variable's delete function aren't available at SQLite!");
    }

    @Override
    public void databaseLoad(@NotNull Database database) {
        if (!(database.getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This database's type isn't an SQLite database!");
        }

        File file = ((SQLiteDatabase) database).getFile();

        try {
            if (!file.createNewFile() && !file.exists()) {
                throw new RuntimeException("(¹) Couldn't create SQLite file at '" + file + "'");
            }

            Class.forName("org.sqlite.JDBC");
            ((SQLiteDatabase) database).setConnection(DriverManager.getConnection("jdbc:sqlite:" + file));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load SQLiteDatabaseType '" + database.getName() + "'. File '" + file + "'", e);
        }
    }

    @Override
    public void databaseDelete(@NotNull Database database) {
        if (!(database.getDatabaseType() instanceof SQLiteDatabaseType)) {
            throw new IllegalArgumentException("This database's type isn't an SQLite database!");
        }

        //noinspection ResultOfMethodCallIgnored
        new File(path.getName() + File.separator + database.getName()).delete();
    }
}
