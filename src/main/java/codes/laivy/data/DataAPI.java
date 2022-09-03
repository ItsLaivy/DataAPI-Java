package codes.laivy.data;

import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Table;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.query.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAPI {

    public static final Map<DatabaseType, List<Database>> DATABASES = new HashMap<>();
    public static final Map<Database, List<Table>> TABLES = new HashMap<>();
    public static final Map<Table, List<Variable>> VARIABLES = new HashMap<>();
    public static final Map<Table, List<Receptor>> RECEPTORS = new HashMap<>();

    public static final Map<Receptor, List<InactiveVariable>> INACTIVE_VARIABLES = new HashMap<>();
    public static final Map<Receptor, List<ActiveVariable>> ACTIVE_VARIABLES = new HashMap<>();

    public static final Map<Database, Integer> DATABASE_QUERIES = new HashMap<>();

    // ----/-/---- //

    public static final String VERSION = "1.0";
    public static boolean DEBUG = false;

    /**
     * If true, when tries to create a database/table or/and variables that already exists
     * will throw an exception
     */
    public static boolean EXISTS_ERROR = true;

    public static void main(String[] args) {
        System.out.println("Thanks for using the DataAPI " + VERSION + " :)");
        System.out.println("Github link: https://github.com/ItsLaivy/DataAPI-Java");
    }

    public static String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
    }

    @Nullable
    public static Database getDatabase(@NotNull DatabaseType databaseType, @NotNull String name) {
        for (Database database : DATABASES.get(databaseType)) {
            if (database.getName().equals(name)) {
                return database;
            }
        }
        return null;
    }
    @Nullable
    public static Table getTable(@NotNull Database database, @NotNull String name) {
        for (Table table : TABLES.get(database)) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }
    @Nullable
    public static Receptor getReceptor(@NotNull Table table, @NotNull String bruteId) {
        for (Receptor receptor : RECEPTORS.get(table)) {
            if (receptor.getBruteId().equals(bruteId)) {
                return receptor;
            }
        }
        return null;
    }

    @Nullable
    public static Variable getVariable(@NotNull Table table, @NotNull String name) {
        for (Variable variable : VARIABLES.get(table)) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }
    @Nullable
    public static ActiveVariable getActiveVariable(@NotNull Receptor receptor, @NotNull String variable) {
        for (ActiveVariable activeVariable : ACTIVE_VARIABLES.get(receptor)) {
            if (activeVariable.getVariable().getName().equals(variable)) {
                return activeVariable;
            }
        }
        return null;
    }
    @Nullable
    public static InactiveVariable getInactiveVariable(@NotNull Receptor receptor, @NotNull String variable) {
        for (InactiveVariable inactiveVariable : INACTIVE_VARIABLES.get(receptor)) {
            if (inactiveVariable.getVariable().equals(variable)) {
                return inactiveVariable;
            }
        }
        return null;
    }

}
