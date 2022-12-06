package codes.laivy.data.sql.mysql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.sql.*;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.*;
import java.util.Map;
import java.util.Objects;

public class MySQLDatabaseType extends SQLDatabaseType {

    protected @Nullable Connection connection;

    protected final @NotNull String user;
    protected final @NotNull String password;
    protected final int port;
    protected final @NotNull String address;

    public MySQLDatabaseType(@NotNull String user, @NotNull String password, int port, @NotNull String address) {
        super("MYSQL");

        this.user = user;
        this.password = password;
        this.port = port;
        this.address = address;

        open();
    }

    public void open() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/?autoReconnect=true&failOverReadOnly=false&verifyServerCertificate=false", user, password);

            if (getConnection().isClosed()) {
                throw new IllegalStateException("The database's connection is closed!");
            }
        } catch (Throwable e) {
            if (e.getClass().equals(ClassNotFoundException.class)) {
                throw new RuntimeException("Couldn't get the JDBC Drivers for the MySQL connection!", e);
            } else {
                throw new RuntimeException("Couldn't authenticate to MySQLDatabase", e);
            }
        }
    }
    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                throwError(e);
            }
        }
    }

    public @NotNull String getUser() {
        return user;
    }
    public @NotNull String getPassword() {
        return password;
    }
    public int getPort() {
        return port;
    }
    public @NotNull String getAddress() {
        return address;
    }

    @NotNull
    public synchronized Connection getConnection() {
        return Objects.requireNonNull(connection);
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
        new MySQLStatement(this, query).execute();
    }

    @Override
    public @NotNull Map<String, String> data(@NotNull SQLReceptor receptor) {
        MySQLResult query = (MySQLResult) receptor.getTable().getDatabase().query("SELECT * FROM " + receptor.getTable().getName() + " WHERE bruteid = '" + receptor.getBruteId() + "'");
        Map<String, String> results = query.results();
        query.close();
        return results;
    }

    @Override
    public void receptorLoad(@NotNull SQLReceptor receptor) {
        Map<String, String> data = data(receptor);
        receptor.setNew(data.isEmpty());

        if (data.isEmpty()) {
            receptor.getDatabase().query("INSERT INTO " + receptor.getTable().getName() + " (name,bruteid,last_update) VALUES ('" + receptor.getName() + "','" + receptor.getBruteId() + "','" + DataAPI.getDate() + "')");
            data = data(receptor);
        }

        int row = 0;
        for (Map.Entry<String, String> map : data.entrySet()) {
            if (row > 3) {
                new InactiveVariable(receptor, map.getKey(), map.getValue());
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
        receptor.getDatabase().query("DELETE FROM " + receptor.getTable().getName() + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void save(@NotNull SQLReceptor receptor) {
        StringBuilder query = new StringBuilder();
        for (ActiveVariable variable : receptor.getActiveVariables()) {
            if (!variable.getVariable().isSaveToDatabase()) {
                continue;
            }

            String data;
            if (variable.getVariable().isSerialize()) {
                if (!(variable.getValue() instanceof Serializable)) {
                    throw new IllegalArgumentException("The variable is a serializable variable, but the current value isn't a instance of Serializable!");
                }

                data = Variable.serialize((Serializable) variable.getValue());
            } else {
                data = variable.getValue() != null ? variable.getValue().toString() : "";
            }

            query.append("`").append(variable.getVariable().getName()).append("`='").append(data).append("',");
        }
        query.append("`last_update`='").append(DataAPI.getDate()).append("'");

        receptor.getDatabase().query("UPDATE " + receptor.getTable().getName() + " SET " + query + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    @Override
    public void tableLoad(@NotNull SQLTable sqlTable) {
        sqlTable.getDatabase().query("CREATE TABLE " + sqlTable.getName() + " (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128), bruteid VARCHAR(128), last_update VARCHAR(21));");
    }

    @Override
    public void tableDelete(@NotNull SQLTable sqlTable) {
        sqlTable.getDatabase().query("DROP TABLE " + sqlTable.getName());
    }

    @Override
    public void variableLoad(@NotNull SQLVariable variable) {
        try {
            if (variable.isSaveToDatabase()) {
                String data;
                if (variable.isSerialize()) {
                    if (!(variable.getDefaultValue() instanceof Serializable)) {
                        throw new IllegalArgumentException("A opção de serialização está ativada mas o valor padrão não extende Serializable!");
                    }

                    data = Variable.serialize((Serializable) variable.getDefaultValue());
                } else {
                    if (variable.getDefaultValue() != null) {
                        data = variable.getDefaultValue().toString();
                    } else {
                        data = "<!NULL>";
                    }
                }

                variable.getDatabase().query("ALTER TABLE " + variable.getTable().getName() + " ADD COLUMN " + variable.getName() + " MEDIUMTEXT DEFAULT '" + data + "';");
            }
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void variableDelete(@NotNull SQLVariable variable) {
        variable.getDatabase().query("ALTER TABLE " + variable.getTable().getName() + " DROP COLUMN '" + variable.getName());
    }

    @Override
    public void databaseLoad(@NotNull Database database) {
        query("CREATE DATABASE " + database.getName());
    }

    @Override
    public void databaseDelete(@NotNull Database database) {
        query("DROP DATABASE " + database.getName());
    }
}
