package codes.laivy.data;

import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.api.table.Tableable;
import codes.laivy.data.redis.RedisDatabase;
import codes.laivy.data.redis.RedisTable;
import codes.laivy.data.redis.RedisVariable;
import codes.laivy.data.sql.SQLReceptor;
import codes.laivy.data.sql.SQLTable;
import codes.laivy.data.sql.SQLVariable;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

import static codes.laivy.data.sql.SQLTable.*;

public class DataAPI {

    public static final @NotNull Map<@NotNull DatabaseType<?, ?>, @NotNull Set<@NotNull Database>> DATABASES = new HashMap<>();
    public static final @NotNull Map<@NotNull Database, @NotNull Set<@NotNull Table>> TABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull Database, @NotNull Set<@NotNull Variable>> VARIABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull Database, Set<@NotNull Receptor>> RECEPTORS = new HashMap<>();

    public static final @NotNull Map<@NotNull Receptor, @NotNull Set<@NotNull InactiveVariable>> INACTIVE_VARIABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull Receptor, @NotNull Set<@NotNull ActiveVariable>> ACTIVE_VARIABLES = new HashMap<>();

    // ----/-/---- //

    public static final @NotNull String VERSION = "5.0";
    public static boolean DEBUG = false;

    /**
     * If true, when tries to create a database/table or/and variables that already exists
     * will throw an exception
     * <p>
     * Not included for receptors.
     */
    public static boolean EXISTS_ERROR = false;

    public static void main(String[] args) {
        System.out.println("Thanks for using the DataAPI " + VERSION + " :)");
        System.out.println("Github link: https://github.com/ItsLaivy/DataAPI-Java");
    }

    public static @NotNull String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
    }

    @Nullable
    public static Database getDatabase(@NotNull DatabaseType<?, ?> databaseType, @NotNull String name) {
        if (DATABASES.containsKey(databaseType)) {
            for (Database database : DATABASES.get(databaseType)) {
                if (database.getName().equals(name)) {
                    return database;
                }
            }
        }
        return null;
    }
    @Nullable
    public static Table getTable(@NotNull Database database, @NotNull String name) {
        if (TABLES.containsKey(database)) {
            for (Table table : TABLES.get(database)) {
                if (table.getName().equals(name)) {
                    return table;
                }
            }
        }
        return null;
    }
    public static @NotNull Set<Receptor> getReceptor(@NotNull Database database, @NotNull String bruteId) {
        Set<Receptor> receptors = new HashSet<>();
        if (RECEPTORS.containsKey(database)) {
            for (Receptor receptor : RECEPTORS.get(database)) {
                if (receptor.getBruteId().equals(bruteId) && receptor.getDatabase().equals(database)) {
                    receptors.add(receptor);
                }
            }
        }
        return receptors;
    }
    public static @Nullable SQLReceptor getSQLReceptor(@NotNull SQLTable table, @NotNull String bruteId) {
        if (SQL_RECEPTORS.containsKey(table)) {
            for (SQLReceptor receptor : SQL_RECEPTORS.get(table)) {
                if (receptor.getBruteId().equals(bruteId)) {
                    return receptor;
                }
            }
        }
        return null;
    }
    public static @Nullable RedisTable getRedisTable(@NotNull RedisDatabase database, @NotNull String name) {
        return (RedisTable) getTable(database, name);
    }
    public static @Nullable Variable getVariable(@NotNull Database database, @NotNull String name) {
        if (VARIABLES.containsKey(database)) {
            for (Variable variable : VARIABLES.get(database)) {
                if (variable.getName().equals(name)) {
                    return variable;
                }
            }
        }
        return null;
    }
    public static @Nullable SQLVariable getSQLVariable(@NotNull SQLTable SQLTable, @NotNull String name) {
        if (SQL_VARIABLES.containsKey(SQLTable)) {
            for (SQLVariable variable : SQL_VARIABLES.get(SQLTable)) {
                if (variable.getName().equals(name)) {
                    return variable;
                }
            }
        }
        return null;
    }
    public static @Nullable RedisVariable getRedisVariable(@NotNull RedisDatabase database, @NotNull String name, @Nullable RedisTable table) {
        if (RedisDatabase.VARIABLES.containsKey(database)) {
            for (RedisVariable variable : RedisDatabase.VARIABLES.get(database)) {
                if (variable.getName().equals(name)) {
                    if (!(table != null && variable instanceof Tableable && ((Tableable) variable).getTable().equals(table))) {
                        continue;
                    }

                    return variable;
                }
            }
        }
        return null;
    }
    public static @Nullable ActiveVariable getActiveVariable(@NotNull Receptor receptor, @NotNull String variable) {
        if (!receptor.isLoaded()) {
            throw new IllegalStateException("This receptor '" + receptor.getBruteId() + "' isn't loaded.");
        }

        if (ACTIVE_VARIABLES.containsKey(receptor)) {
            for (ActiveVariable activeVariable : ACTIVE_VARIABLES.get(receptor)) {
                if (activeVariable.getVariable().getName().equals(variable)) {
                    return activeVariable;
                }
            }
        }
        return null;
    }
    public static @Nullable InactiveVariable getInactiveVariable(@NotNull Receptor receptor, @NotNull String variable) {
        if (!receptor.isLoaded()) {
            throw new IllegalStateException("This receptor '" + receptor.getBruteId() + "' isn't loaded.");
        }

        if (INACTIVE_VARIABLES.containsKey(receptor)) {
            for (InactiveVariable inactiveVariable : INACTIVE_VARIABLES.get(receptor)) {
                if (inactiveVariable.getVariable().equals(variable)) {
                    return inactiveVariable;
                }
            }
        }
        return null;
    }

}
