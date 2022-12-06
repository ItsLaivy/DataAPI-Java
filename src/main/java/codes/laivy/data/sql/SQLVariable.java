package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SQLVariable extends Variable {

    private final @NotNull SQLTable sqlTable;

    public SQLVariable(@NotNull String name, @NotNull SQLTable sqlTable, @Nullable Object defaultValue) {
        this(name, sqlTable, defaultValue, true, true);
    }

    public SQLVariable(@NotNull String name, @NotNull SQLTable sqlTable, @Nullable Object defaultValue, boolean serialize, boolean saveToDatabase) {
        super(name, sqlTable.getDatabase(), defaultValue, serialize, saveToDatabase);
        this.sqlTable = sqlTable;

        if (DataAPI.getSQLVariable(sqlTable, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A variable named '" + name + "' at the table '" + getTable().getName() + "' in the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')' already exists!");
            return;
        }

        getDatabase().getDatabaseType().variableLoad(this);

        SQLTable.SQL_VARIABLES.get(sqlTable).add(this);

        for (SQLReceptor receptor : SQLTable.SQL_RECEPTORS.get(sqlTable)) {
            new ActiveVariable(this, receptor, defaultValue);
        }
    }

    @Override
    public @NotNull SQLDatabase getDatabase() {
        return (SQLDatabase) super.getDatabase();
    }

    public void delete() {
        for (SQLReceptor receptor : SQLTable.SQL_RECEPTORS.get(getTable())) {
            DataAPI.ACTIVE_VARIABLES.get(receptor).removeIf(activeVariable -> activeVariable.getVariable().equals(this));
            DataAPI.INACTIVE_VARIABLES.get(receptor).removeIf(inactiveVariable -> inactiveVariable.getVariable().equals(this.getName()));
        }

        SQLTable.SQL_VARIABLES.get(getTable()).remove(this);
        getTable().getDatabase().getDatabaseType().variableDelete(this);
    }

    public @NotNull SQLTable getTable() {
        return sqlTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLVariable)) return false;
        SQLVariable that = (SQLVariable) o;
        return sqlTable.equals(that.sqlTable) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlTable, getName());
    }
}
