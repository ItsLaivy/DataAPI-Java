package codes.laivy.data.redis;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.api.table.Tableable;
import codes.laivy.data.redis.receptor.RedisReceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;

public class RedisVariable extends Variable {

    public RedisVariable(@NotNull RedisDatabase database, @NotNull String name, @Nullable Object defaultValue) {
        this(database, name, defaultValue, true);
    }
    public RedisVariable(@NotNull RedisDatabase database, @NotNull String name, @Nullable Object defaultValue, boolean serialize) {
        super(name, database, defaultValue, serialize, true);

        if (DataAPI.getRedisVariable(database, name, null) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A redis variable named '" + name + "' in the database '" + getDatabase().getName() + " ('" + getDatabase().getDatabaseType().getName() + "')' already exists!");
            return;
        }

        RedisDatabase.VARIABLES.putIfAbsent(database, new LinkedHashSet<>());
        RedisDatabase.VARIABLES.get(database).add(this);
    }

    @Override
    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    public void delete() {
        getDatabase().getDatabaseType().variableDelete(this);
        RedisDatabase.VARIABLES.get(getDatabase()).remove(this);
    }

    public @NotNull String getRedisVariableName(@NotNull RedisReceptor receptor) {
        if (this instanceof Tableable && receptor instanceof Tableable) {
            Table receptorTable = ((Tableable) receptor).getTable();
            Table variableTable = ((Tableable) this).getTable();

            if (receptorTable.equals(variableTable)) {
                return "DataAPI:" + getDatabase().getName() + "_" + variableTable.getName() + "_" + getName() + "_" + receptor.getBruteId();
            } else {
                throw new IllegalStateException("This receptor's table '" + receptorTable.getName() + "' doesn't matches with the variable's table '" + variableTable.getName() + "'.");
            }
        } else if (this instanceof Tableable) {
            throw new IllegalStateException("This variable is a tableable variable, but the receptor isn't.");
        } else if (receptor instanceof Tableable) {
            throw new IllegalStateException("This receptor is a tableable receptor, but the variable isn't.");
        } else {
            return "DataAPI:" + getDatabase().getName() + "_" + getName() + "_" + receptor.getBruteId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisVariable)) return false;
        RedisVariable that = (RedisVariable) o;
        return getDatabase().equals(that.getDatabase()) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabase(), getName());
    }
}
