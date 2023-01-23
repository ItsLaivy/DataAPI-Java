package codes.laivy.data.redis.lettuce;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class RedisDatabase extends Database {

    public static final @NotNull Map<@NotNull RedisDatabase, @NotNull Set<@NotNull RedisTable>> TABLES = new LinkedHashMap<>();
    public static final @NotNull Map<@NotNull RedisDatabase, @NotNull Set<@NotNull RedisReceptor>> RECEPTORS = new LinkedHashMap<>();
    public static final @NotNull Map<@NotNull RedisDatabase, @NotNull Set<@NotNull RedisVariable>> VARIABLES = new LinkedHashMap<>();

    private final @NotNull Set<@NotNull String> keys = new LinkedHashSet<>();

    public RedisDatabase(@NotNull RedisDatabaseType redisDatabaseType, @NotNull String name) {
        super(redisDatabaseType, name);
    }

    public boolean has(@NotNull String key) {
        key = "DataAPI:" + getName() + "_" + key;
        return getDatabaseType().getConnection().sync().exists(key);
    }
    public void remove(@NotNull String key) {
        key = "DataAPI:" + getName() + "_" + key;

        keys.remove(key);
        getDatabaseType().getConnection().sync().del(key);
    }
    public void set(@NotNull String key, @Nullable Serializable value) {
        key = "DataAPI:" + getName() + "_" + key;

        keys.add(key);
        getDatabaseType().getConnection().sync().set(key, Variable.serialize(value));
    }
    public @Nullable Serializable get(@NotNull String key) {
        key = "DataAPI:" + getName() + "_" + key;

        if (getDatabaseType().getConnection().sync().exists(key)) {
            return Variable.unserialize(getDatabaseType().getConnection().sync().get(key));
        }
        throw new NullPointerException("Couldn't find this key at the database");
    }
    @ApiStatus.Internal
    public @NotNull Set<@NotNull String> getKeys() {
        return keys;
    }

    @Override
    public @NotNull RedisVariable[] getVariables() {
        List<Variable> dVars = new LinkedList<>(DataAPI.VARIABLES.get(this));
        RedisVariable[] variables = new RedisVariable[dVars.size()];
        for (int row = 0; row < dVars.size(); row++) {
            variables[row] = (RedisVariable) dVars.get(row);
        }
        return variables;
    }
    @Override
    public @NotNull RedisReceptor[] getReceptors() {
        List<Receptor> dVars = new LinkedList<>(DataAPI.RECEPTORS.get(this));
        RedisReceptor[] receptors = new RedisReceptor[dVars.size()];
        for (int row = 0; row < dVars.size(); row++) {
            receptors[row] = (RedisReceptor) dVars.get(row);
        }
        return receptors;
    }

    @Override
    public @NotNull RedisDatabaseType getDatabaseType() {
        return (RedisDatabaseType) super.getDatabaseType();
    }

    public @NotNull Set<@NotNull String> getDatabaseKeys() {
        Set<String> strings = new LinkedHashSet<>();
        for (RedisReceptor receptor : getReceptors()) {
            strings.addAll(receptor.getRedisKeys());
        }
        return strings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisDatabase)) return false;
        RedisDatabase database = (RedisDatabase) o;
        return getDatabaseType().equals(database.getDatabaseType()) && getName().equals(database.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabaseType(), getName());
    }
}
