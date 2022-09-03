package codes.laivy.data.sql.mysql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Table;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Map;

public class MySQLDatabaseType extends DatabaseType {

    protected final Connection connection;

    protected final String user;
    protected final String password;
    protected final int port;
    protected final String address;

    public MySQLDatabaseType(@NotNull String user, @NotNull String password, int port, @NotNull String address) {
        super("MYSQL");

        this.user = user;
        this.password = password;
        this.port = port;
        this.address = address;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&verifyServerCertificate=false", user, password);

            if (getConnection().isClosed()) {
                throw new IllegalStateException("The database's connection is closed!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't authenticate to MySQLDatabase", e);
        }
    }

    public String getUser() {
        return user;
    }
    public String getPassword() {
        return password;
    }
    public int getPort() {
        return port;
    }
    public String getAddress() {
        return address;
    }

    @NotNull
    public synchronized Connection getConnection() {
        return connection;
    }

    @Override
    public @NotNull String[] suppressedErrors() {
        return new String[] {
                "database exists",
                "already exists",
                "Duplicate column name"
        };
    }

    private void query(@NotNull String query) {
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.executeQuery();
        } catch (SQLException ex) {
            if (ex.getMessage().equals("Statement.executeQuery() cannot issue statements that do not produce result sets.")) {
                try (PreparedStatement statement = getConnection().prepareStatement(query)) {
                    statement.executeUpdate();
                } catch (SQLException ex2) {
                    throwError(ex2);
                }
            } else {
                throwError(ex);
            }
        }
    }

    @Override
    public @NotNull Map<String, String> data(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an MySQL database!");
        }

        return ((MySQLDatabase) receptor.getTable().getDatabase()).query("SELECT * FROM " + receptor.getTable().getDatabase().getName() + "." + receptor.getTable().getName() + " WHERE bruteid = '" + receptor.getBruteId() + "'").results();
    }

    @Override
    public void receptorLoad(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an MySQL database!");
        }

        Map<String, String> data = data(receptor);
        if (data.isEmpty()) {
            query("INSERT INTO " + receptor.getTable().getDatabase().getName() + "." + receptor.getTable().getName() + " (name,bruteid,last_update) VALUES ('" + receptor.getName() + "','" + receptor.getBruteId() + "','" + DataAPI.getDate() + "')");
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
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an MySQL database!");
        }

        query("DELETE FROM " + receptor.getTable().getDatabase().getName() + "." + receptor.getTable().getName() + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void save(@NotNull Receptor receptor) {
        if (!(receptor.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This receptor's database isn't an MySQL database!");
        }

        StringBuilder query = new StringBuilder();
        for (ActiveVariable variable : receptor.getActiveVariables()) {
            query.append("`").append(variable.getVariable().getName()).append("`='").append(Variable.serialize(variable.getValue())).append("',");
        }
        query.append("`last_update`='").append(DataAPI.getDate()).append("'");

        query("UPDATE " + receptor.getTable().getDatabase().getName() + "." + receptor.getTable().getName() + " SET " + query + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void tableLoad(@NotNull Table table) {
        if (!(table.getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This table's database isn't an MySQL database!");
        }

        try {
            query("CREATE TABLE " + table.getDatabase().getName() + "." + table.getName() + " (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128), bruteid VARCHAR(128), last_update VARCHAR(21));");
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void tableDelete(@NotNull Table table) {
        if (!(table.getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This table's database isn't an MySQL database!");
        }

        query("DROP TABLE " + table.getDatabase().getName() + "." + table.getName());
    }

    @Override
    public void variableLoad(@NotNull Variable variable) {
        if (!(variable.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This variable's database isn't an MySQL database!");
        }

        try {
            query("ALTER TABLE " + variable.getTable().getDatabase().getName() + "." + variable.getTable().getName() + " ADD COLUMN " + variable.getName() + " MEDIUMTEXT DEFAULT '" + Variable.serialize(variable.getDefaultValue()) + "';");
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void variableDelete(@NotNull Variable variable) {
        if (!(variable.getTable().getDatabase().getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This variable's database isn't an MySQL database!");
        }

        try {
            query("ALTER TABLE " + variable.getTable().getDatabase().getName() + "." + variable.getTable().getName() + " DROP COLUMN '" + variable.getName());
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void databaseLoad(@NotNull Database database) {
        if (!(database.getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This database isn't an MySQL database!");
        }

        try {
            query("CREATE DATABASE " + database.getName());
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void databaseDelete(@NotNull Database database) {
        if (!(database.getDatabaseType() instanceof MySQLDatabaseType)) {
            throw new IllegalArgumentException("This database isn't an MySQL database!");
        }

        try {
            query("DROP DATABASE " + database.getName());
        } catch (Throwable e) {
            throwError(e);
        }
    }
}
