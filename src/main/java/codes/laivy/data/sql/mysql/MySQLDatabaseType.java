package codes.laivy.data.sql.mysql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.sql.*;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

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

    public @NotNull MySQLStatement query(@NotNull String query) {
        return new MySQLStatement(this, query);
    }

    @Override
    public @NotNull Map<String, Object> receptorData(@NotNull SQLReceptor receptor) {
        MySQLResult query = (MySQLResult) receptor.getTable().getDatabase().query("SELECT * FROM `" + receptor.getTable().getName() + "` WHERE bruteid = '" + receptor.getBruteId() + "'");

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
            receptor.getDatabase().query("INSERT INTO `" + receptor.getTable().getName() + "` (name,bruteid,last_update) VALUES ('" + receptor.getName() + "','" + receptor.getBruteId() + "','" + DataAPI.getDate() + "')");
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
        receptor.getDatabase().query("DELETE FROM `" + receptor.getTable().getName() + "` WHERE bruteid = '" + receptor.getBruteId() + "'");
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
                        throw new IllegalArgumentException("The serialization option are enabled, but the value isn't a instance of Serializable!");
                    }

                    data = Variable.serialize((Serializable) variable.getValue());
                } else {
                    data = variable.getValue().toString();
                }
            } else {
                data = "NULL";
            }

            query.append("`").append(variable.getVariable().getName()).append("`=").append(data).append(",");
        }
        query.append("`last_update`='").append(DataAPI.getDate()).append("',`name`='").append(receptor.getName()).append("',`id`=").append(receptor.getId());

        receptor.getDatabase().query("UPDATE `" + receptor.getTable().getName() + "` SET " + query + " WHERE bruteid = '" + receptor.getBruteId() + "'");
    }

    public @NotNull MySQLDatabase[] getDatabases() {
        Set<MySQLDatabase> databases = new LinkedHashSet<>();
        for (@NotNull Database database : DataAPI.DATABASES.get(this)) {
            databases.add((MySQLDatabase) database);
        }
        return databases.toArray(new MySQLDatabase[0]);
    }

    @Override
    public @NotNull Receptor[] receptorList() {
        Set<Receptor> receptors = new LinkedHashSet<>();
        for (SQLTable table : getTables()) {
            MySQLResult query = (MySQLResult) table.getDatabase().query("SELECT `name`,`bruteid` FROM `" + table.getName() + "`");
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
    public void tableLoad(@NotNull SQLTable table) {
        table.getDatabase().query("CREATE TABLE `" + table.getName() + "` (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128), bruteid VARCHAR(128), last_update VARCHAR(21));");
    }

    @Override
    public void tableDelete(@NotNull SQLTable table) {
        table.getDatabase().query("DROP TABLE `" + table.getName() + "`");
    }

    /**
     * @return the version of the SQL server
     */
    public @NotNull String getVersion() {
        return (String) new LinkedList<>(query("SELECT VERSION();").execute().results()).get(0).get("VERSION()");
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

                // Size
                String size;
                if (variable.getSize() == SQLVariable.Size.TINY) {
                    size = "TINYTEXT";
                } else if (variable.getSize() == SQLVariable.Size.NORMAL) {
                    size = "TEXT";
                } else if (variable.getSize() == SQLVariable.Size.MEDIUM) {
                    size = "MEDIUMTEXT";
                } else if (variable.getSize() == SQLVariable.Size.BIG) {
                    size = "BIGTEXT";
                } else {
                    throw new UnsupportedOperationException("Unknown size type '" + variable.getSize().name() + "'");
                }
                //

                variable.getDatabase().query("ALTER TABLE `" + variable.getTable().getName() + "` ADD COLUMN `" + variable.getName() + "` " + size + " DEFAULT " + data + ";");
                variable.getDatabase().query("ALTER TABLE `" + variable.getTable().getName() + "` MODIFY `" + variable.getName() + "` " + size + " DEFAULT " + data + ";");
            }
        } catch (Throwable e) {
            throwError(e);
        }
    }

    @Override
    public void variableDelete(@NotNull SQLVariable variable) {
        variable.getDatabase().query("ALTER TABLE `" + variable.getTable().getName() + "` DROP COLUMN `" + variable.getName() + "`");
    }

    @Override
    public void databaseLoad(@NotNull Database database) {
        query("CREATE DATABASE `" + database.getName() + "`").execute();
    }

    @Override
    public void databaseDelete(@NotNull Database database) {
        query("DROP DATABASE `" + database.getName() + "`").execute();
    }

}
