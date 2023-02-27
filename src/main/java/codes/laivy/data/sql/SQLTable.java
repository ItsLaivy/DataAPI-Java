package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SQLTable extends Table {

    public static final @NotNull Map<@NotNull SQLTable, @NotNull Set<@NotNull SQLVariable>> SQL_VARIABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull SQLTable, @NotNull Set<@NotNull SQLReceptor>> SQL_RECEPTORS = new HashMap<>();

    // ---/-/--- //

    public SQLTable(@NotNull SQLDatabase database, @NotNull String name) {
        super(database, name);

        if (DataAPI.getTable(database, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A table named '" + name + "' from database '" + database.getName() + " ('" + database.getDatabaseType().getName() + "')' already exists!");
            return;
        }

        database.getDatabaseType().tableLoad(this);

        DataAPI.TABLES.get(database).add(this);
        SQL_VARIABLES.put(this, new HashSet<>());
        SQL_RECEPTORS.put(this, new HashSet<>());
    }

    @NotNull
    public SQLDatabase getDatabase() {
        return (SQLDatabase) super.getDatabase();
    }

    @Override
    public void delete() {
        // Unload receptors
        for (Receptor receptor : getReceptors()) {
            receptor.unload(false);
        }
        // Unload variables
        for (Variable variable : getVariables()) {
            variable.delete();
        }

        getDatabase().getDatabaseType().tableDelete(this);
    }

    @Override
    public @NotNull SQLReceptor[] getReceptors() {
        SQLReceptor[] receptors = new SQLReceptor[SQL_RECEPTORS.get(this).size()];
        int row = 0;
        for (SQLReceptor receptor : SQL_RECEPTORS.get(this)) {
            receptors[row] = receptor;
            row++;
        }
        return receptors;
    }

    @Override
    public @NotNull SQLVariable[] getVariables() {
        SQLVariable[] variables = new SQLVariable[SQL_VARIABLES.get(this).size()];
        int row = 0;
        for (SQLVariable variable : SQL_VARIABLES.get(this)) {
            variables[row] = variable;
            row++;
        }
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLTable)) return false;
        SQLTable SQLTable = (SQLTable) o;
        return getDatabase().equals(SQLTable.getDatabase()) && getName().equals(SQLTable.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabase(), getName());
    }
}
