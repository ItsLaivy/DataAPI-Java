package codes.laivy.data.sql.sqlite;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.sql.*;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.sql.DriverManager;
import java.util.*;

public class SQLiteDatabaseType extends SQLDatabaseType {

    public final @NotNull File path;

    public SQLiteDatabaseType(@NotNull String path) {
        this(new File(path));
    }

    public SQLiteDatabaseType(@NotNull File path) {
        super("SQLITE");
        this.path = path;
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
    }

    public @NotNull File getPath() {
        return path;
    }

    private void query(@NotNull SQLiteDatabase database, @NotNull String query) {
        try {
            database.query(query).close();
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
    public @NotNull Map<String, Object> receptorData(@NotNull SQLReceptor receptor) {
        SQLiteResult query = (SQLiteResult) receptor.getTable().getDatabase().query("SELECT * FROM '" + receptor.getTable().getName() + "' WHERE bruteid = '" + receptor.getBruteId() + "'");

        @NotNull Set<Map<String, Object>> results = query.results();
        query.close();

        if (results.isEmpty()) {
            return new LinkedHashMap<>();
        } else if (results.size() == 1) {
            return new LinkedList<>(results).getFirst();
        } else {
            throw new UnsupportedOperationException("Multiples receptors with same brute id '" + receptor.getBruteId() + "' founded inside table '" + receptor.getTable().getName() + "' at database '" + receptor.getDatabase().getName() + "'");
        }
    }

    @Override
    public void receptorLoad(@NotNull SQLReceptor receptor) {
        Map<String, Object> data = receptorData(receptor);
        receptor.setNew(data.isEmpty());

        if (data.isEmpty()) {
            query((SQLiteDatabase) receptor.getTable().getDatabase(), "INSERT INTO '" + receptor.getTable().getName() + "' (name,bruteid,last_update) VALUES ('" + receptor.getName() + "','" + receptor.getBruteId() + "','" + DataAPI.getDate() + "')");
            data = receptorData(receptor);
        }

        int row = 0;
        for (Map.Entry<String, Object> map : data.entrySet()) {
            if (map.getKey().equals("id")) {
                receptor.setId((int) map.getValue());
            } else if (row > 3) {
                new InactiveVariable(receptor, map.getKey(), (String) map.getValue());
            }
            row++;
        }
        for (Variable variable : Variable.TEMPORARY_VARIABLES) {
            if (variable instanceof SQLVariable) {
                SQLVariable var = (SQLVariable) variable;
                if (var.getTable().equals(receptor.getTable())) {
                    new ActiveVariable(variable, receptor, variable.getDefaultValue());
                }
            }
        }
    }

    @Override
    public void receptorDelete(@NotNull SQLReceptor receptor) {
        query((SQLiteDatabase) receptor.getTable().getDatabase(), "DELETE FROM '" + receptor.getTable().getName() + "' WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void receptorSave(@NotNull SQLReceptor receptor) {
        StringBuilder query = new StringBuilder();
        for (ActiveVariable variable : receptor.getActiveVariables()) {
            if (!variable.getVariable().isSaveToDatabase()) {
                continue;
            }

            String data;
            if (variable.getValue() != null) {
                if (variable.getVariable().isSerialize()) {
                    if (!(variable.getValue() instanceof Serializable)) {
                        throw new IllegalArgumentException("The variable is a serializable variable, but the current value isn't a instance of Serializable!");
                    }

                    data = "'" + Variable.serialize((Serializable) variable.getValue()) + "'";
                } else {
                    data = "'" + variable.getValue().toString() + "'";
                }
            } else {
                data = "NULL";
            }

            query.append("`").append(variable.getVariable().getName()).append("`='").append(data).append("',");
        }
        query.append("`last_update`='").append(DataAPI.getDate()).append("',`name`='").append(receptor.getName()).append("',`id`=").append(receptor.getId());

        query((SQLiteDatabase) receptor.getTable().getDatabase(), "UPDATE '" + receptor.getTable().getName() + "' SET " + query + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public @NotNull Receptor[] receptorList() {
        Set<Receptor> receptors = new LinkedHashSet<>();
        for (SQLTable table : getTables()) {
            SQLiteResult query = (SQLiteResult) table.getDatabase().query("SELECT 'name','bruteid' FROM '" + table.getName() + "'");
            Set<Map<String, Object>> data = query.results();
            query.close();

            f1:
            for (Map<String, Object> map : data) {
                String name = (String) map.get("name");
                String bruteId = (String) map.get("bruteid");

                for (SQLReceptor receptor : SQLTable.SQL_RECEPTORS.get(table)) {
                    if (receptor.getBruteId().equals(bruteId)) {
                        receptors.add(receptor);
                        continue f1;
                    }
                }
                receptors.add(new SQLReceptor(table, name, bruteId));
            }
        }
        return receptors.toArray(new Receptor[0]);
    }

    @Override
    public void tableLoad(@NotNull SQLTable SQLTable) {
        query((SQLiteDatabase) SQLTable.getDatabase(), "CREATE TABLE '" + SQLTable.getName() + "' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, bruteid TEXT, 'last_update' TEXT);");
    }

    @Override
    public void tableDelete(@NotNull SQLTable SQLTable) {
        query((SQLiteDatabase) SQLTable.getDatabase(), "DROP TABLE '" + SQLTable.getName() + "'");
    }

    public @NotNull SQLiteDatabase[] getDatabases() {
        Set<SQLiteDatabase> databases = new LinkedHashSet<>();
        for (@NotNull Database database : DataAPI.DATABASES.get(this)) {
            databases.add((SQLiteDatabase) database);
        }
        return databases.toArray(new SQLiteDatabase[0]);
    }

    @Override
    public void variableLoad(@NotNull SQLVariable variable) {
        try {
            if (variable.isSaveToDatabase()) {
                String data;
                if (variable.getDefaultValue() != null) {
                    if (variable.isSerialize()) {
                        if (!(variable.getDefaultValue() instanceof Serializable)) {
                            throw new IllegalArgumentException("The serialization option are enabled, but the value isn't a instance of Serializable!");
                        }

                        data = "'" + Variable.serialize((Serializable) variable.getDefaultValue()) + "'";
                    } else {
                        data = "'" + variable.getDefaultValue().toString() + "'";
                    }
                } else {
                    data = "NULL";
                }

                query((SQLiteDatabase) variable.getTable().getDatabase(), "ALTER TABLE '" + variable.getTable().getName() + "' ADD COLUMN '" + variable.getName() + "' TEXT DEFAULT " + data + ";");
                query((SQLiteDatabase) variable.getTable().getDatabase(), "ALTER TABLE '" + variable.getTable().getName() + "' MODIFY COLUMN '" + variable.getName() + "' TEXT DEFAULT " + data + ";");
            }
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void variableDelete(@NotNull SQLVariable variable) {
        throw new IllegalStateException("The variable's delete function aren't available at SQLite yet!");
    }

    @Override
    public void databaseLoad(@NotNull Database database) {
        SQLiteDatabase db = (SQLiteDatabase) database;
        File file = db.getFile();

        try {
            if (!file.createNewFile() && !file.exists()) {
                throw new RuntimeException("(¹) Couldn't create SQLite file at '" + file + "'");
            }

            Class.forName("org.sqlite.JDBC");
            db.setConnection(DriverManager.getConnection("jdbc:sqlite:" + file));
        } catch (Exception e) {
            if (e.getClass().equals(ClassNotFoundException.class)) {
                throw new RuntimeException("Couldn't get the JDBC Drivers for the SQLite connection!", e);
            } else {
                throw new RuntimeException("Couldn't load SQLiteDatabaseType '" + database.getName() + "'. File '" + file + "'", e);
            }
        }
    }

    @Override
    public void databaseDelete(@NotNull Database database) {
        //noinspection ResultOfMethodCallIgnored
        new File(path.getName() + File.separator + database.getName()).delete();
    }
}
