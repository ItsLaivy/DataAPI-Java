package codes.laivy.data.redis.lettuce;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Variable;
import codes.laivy.data.utils.ObjectsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;

public class RedisVariable extends Variable {

    private final @Nullable RedisTable table;

    public RedisVariable(@Nullable RedisTable table, @NotNull String name, @NotNull RedisDatabase database, @Nullable Object defaultValue) {
        this(table, name, database, defaultValue, true);
    }
    public RedisVariable(@Nullable RedisTable table, @NotNull String name, @NotNull RedisDatabase database, @Nullable Object defaultValue, boolean serialize) {
        super(name, database, defaultValue, serialize, true);

        this.table = table;
        if (this.table != null && !this.table.getDatabase().equals(database)) {
            throw new IllegalArgumentException("This table's database needs to be the same of the variable's database!");
        }

        if (DataAPI.getRedisVariable(database, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A redis variable named '" + name + "' in the database '" + getDatabase().getName() + " ('" + getDatabase().getDatabaseType().getName() + "')' already exists!");
            return;
        }

        RedisDatabase.VARIABLES.putIfAbsent(database, new LinkedHashSet<>());
        RedisDatabase.VARIABLES.get(database).add(this);

        if (table != null) {
            RedisTable.REDIS_TABLED_VARIABLES.get(table).add(this);
        }
    }

    public @Nullable RedisTable getTable() {
        return table;
    }

    @Override
    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    public void delete() {
        getDatabase().getDatabaseType().variableDelete(this);
        RedisDatabase.VARIABLES.get(getDatabase()).remove(this);
        if (table != null) {
            RedisTable.REDIS_TABLED_VARIABLES.get(table).remove(this);
        }
    }

    public @NotNull String getRedisVariableName(@NotNull RedisReceptor receptor) {
        if (ObjectsUtils.equals(receptor.getTable(), getTable())) {
            if (getTable() != null) {
                return getDatabase().getName() + "_" + getTable().getName() + "_" + getName() + "_" + receptor.getBruteId();
            } else {
                return getDatabase().getName() + "_" + getName() + "_" + receptor.getBruteId();
            }
        } else {
            throw new IllegalStateException("This receptor's table doesn't matches with the variable's table.");
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
