package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Variable;
import codes.laivy.data.query.DataResult;
import codes.laivy.data.query.DataStatement;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class SQLDatabase extends Database {

    public SQLDatabase(@NotNull SQLDatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    @Override
    public @NotNull SQLVariable[] getVariables() {
        List<Variable> dVars = new LinkedList<>(DataAPI.VARIABLES.get(this));
        SQLVariable[] variables = new SQLVariable[dVars.size()];

        for (int row = 0; row < dVars.size(); row++) {
            variables[row] = (SQLVariable) dVars.get(row);
        }

        return variables;
    }

    @Override
    public @NotNull SQLDatabaseType getDatabaseType() {
        return (SQLDatabaseType) super.getDatabaseType();
    }

    protected abstract @NotNull DataStatement statement(String query);
    public abstract @NotNull DataResult query(String query);

    protected abstract void open();
    protected abstract void close();

}
