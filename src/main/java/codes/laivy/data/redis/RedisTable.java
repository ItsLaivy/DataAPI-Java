package codes.laivy.data.redis;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.api.Variable;
import codes.laivy.data.redis.receptor.RedisReceptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RedisTable extends Table {

    public static final @NotNull Map<@NotNull RedisTable, @NotNull Set<@NotNull RedisVariable>> REDIS_TABLED_VARIABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull RedisTable, @NotNull Set<@NotNull RedisReceptor>> REDIS_TABLED_RECEPTORS = new HashMap<>();

    // ---/-/--- //

    public RedisTable(@NotNull RedisDatabase database, @NotNull String name) {
        super(database, name);

        if (DataAPI.getRedisTable(database, name) != null) {
            throw new IllegalStateException("A RedisTable with that properties already exists!");
        }

        RedisDatabase.TABLES.putIfAbsent(database, new LinkedHashSet<>());
        RedisDatabase.TABLES.get(database).add(this);

        REDIS_TABLED_VARIABLES.put(this, new LinkedHashSet<>());
        REDIS_TABLED_RECEPTORS.put(this, new LinkedHashSet<>());
    }

    @Override
    public @NotNull RedisVariable[] getVariables() {
        return REDIS_TABLED_VARIABLES.get(this).toArray(new RedisVariable[0]);
    }

    @Override
    public @NotNull RedisReceptor[] getReceptors() {
        return REDIS_TABLED_RECEPTORS.get(this).toArray(new RedisReceptor[0]);
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

        REDIS_TABLED_VARIABLES.remove(this);
        REDIS_TABLED_RECEPTORS.remove(this);
    }

    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisTable)) return false;
        RedisTable that = (RedisTable) o;
        return getDatabase().equals(that.getDatabase()) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabase(), getName());
    }
}
