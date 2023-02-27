package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.query.DataResult;
import codes.laivy.data.query.DataStatement;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class SQLDatabase extends Database {

    public SQLDatabase(@NotNull SQLDatabaseType databaseType, @NotNull String name) {
        super(databaseType, name);
    }

    @Override
    public @NotNull SQLReceptor[] getAllReceptors() {
        Set<SQLReceptor> receptors = new LinkedHashSet<>();
        for (Receptor receptor : super.getAllReceptors()) {
            receptors.add((SQLReceptor) receptor);
        }
        return receptors.toArray(new SQLReceptor[0]);
    }
    public SQLTable[] getTables() {
        Set<SQLTable> tables = new LinkedHashSet<>();
        for (Table table : DataAPI.TABLES.get(this)) {
            tables.add((SQLTable) table);
        }
        return tables.toArray(new SQLTable[0]);
    }
    @Override
    public @NotNull SQLVariable[] getVariables() {
        Set<SQLVariable> variables = new LinkedHashSet<>();
        for (Variable variable : DataAPI.VARIABLES.get(this)) {
            variables.add((SQLVariable) variable);
        }
        return variables.toArray(new SQLVariable[0]);
    }
    @Override
    public @NotNull SQLReceptor[] getReceptors() {
        Set<SQLReceptor> variables = new LinkedHashSet<>();
        for (Receptor receptor : DataAPI.RECEPTORS.get(this)) {
            variables.add((SQLReceptor) receptor);
        }
        return variables.toArray(new SQLReceptor[0]);
    }

    @Override
    public @NotNull SQLDatabaseType getDatabaseType() {
        return (SQLDatabaseType) super.getDatabaseType();
    }

    @Override
    public void delete() {
        super.delete();
    }

    protected abstract @NotNull DataStatement statement(String query);
    public abstract @NotNull DataResult query(String query);

    protected abstract void open();
    protected abstract void close();

}
