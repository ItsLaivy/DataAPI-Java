package codes.laivy.data.redis.lettuce;

import codes.laivy.data.DataAPI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RedisTable {

    public static final @NotNull Map<@NotNull RedisTable, @NotNull Set<@NotNull RedisVariable>> REDIS_TABLED_VARIABLES = new HashMap<>();
    public static final @NotNull Map<@NotNull RedisTable, @NotNull Set<@NotNull RedisReceptor>> REDIS_TABLED_RECEPTORS = new HashMap<>();

    // ---/-/--- //

    private final @NotNull RedisDatabase database;
    private final @NotNull String name;

    public RedisTable(@NotNull RedisDatabase database, @NotNull String name) {
        this.database = database;
        this.name = name;


        if (DataAPI.getRedisTable(database, name) != null) {
            throw new IllegalStateException("A RedisTable with that properties already exists!");
        }

        RedisDatabase.TABLES.putIfAbsent(database, new LinkedHashSet<>());
        RedisDatabase.TABLES.get(database).add(this);

        REDIS_TABLED_VARIABLES.put(this, new LinkedHashSet<>());
        REDIS_TABLED_RECEPTORS.put(this, new LinkedHashSet<>());
    }

    public @NotNull Set<@NotNull RedisVariable> getVariables() {
        return REDIS_TABLED_VARIABLES.get(this);
    }
    public @NotNull Set<@NotNull RedisReceptor> getReceptors() {
        return REDIS_TABLED_RECEPTORS.get(this);
    }

    public @NotNull RedisDatabase getDatabase() {
        return database;
    }

    public @NotNull String getName() {
        return name;
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
