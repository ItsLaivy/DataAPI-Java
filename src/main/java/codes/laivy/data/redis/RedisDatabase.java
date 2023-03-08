package codes.laivy.data.redis;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.table.Table;
import codes.laivy.data.redis.type.RedisDatabaseType;
import codes.laivy.data.redis.type.lettuce.RedisLettuceDatabaseType;
import codes.laivy.data.redis.receptor.RedisReceptor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class RedisDatabase extends Database {

    public static final @NotNull Map<@NotNull RedisDatabase, @NotNull Set<@NotNull RedisReceptor>> RECEPTORS = new LinkedHashMap<>();
    public static final @NotNull Map<@NotNull RedisDatabase, @NotNull Set<@NotNull RedisVariable>> VARIABLES = new LinkedHashMap<>();

    public RedisDatabase(@NotNull RedisDatabaseType redisDatabaseType, @NotNull String name) {
        super(redisDatabaseType, name);
    }

    public boolean has(@NotNull String key) {
        key = "dataapi_custom:" + getName() + "_" + key;
        return getDatabaseType().getConnection().sync().exists(key);
    }
    public void remove(@NotNull String key) {
        key = "dataapi_custom:" + getName() + "_" + key;
        getDatabaseType().getConnection().sync().del(key);
    }
    public void set(@NotNull String key, @Nullable Serializable value) {
        key = "dataapi_custom:" + getName() + "_" + key;
        getDatabaseType().getConnection().sync().set(key, Variable.serialize(value));
    }
    public @Nullable Serializable get(@NotNull String key) {
        key = "dataapi_custom:" + getName() + "_" + key;

        if (getDatabaseType().getConnection().sync().exists(key)) {
            return Variable.unserialize(getDatabaseType().getConnection().sync().get(key));
        }

        throw new NullPointerException("Couldn't find this key at the database");
    }
    @ApiStatus.Experimental
    public @NotNull Set<@NotNull String> getKeys() {
        return new LinkedHashSet<>(getDatabaseType().getConnection().sync().keys("DataAPI:" + getName() + "_*"));
    }

    @Override
    public @NotNull RedisReceptor[] getAllReceptors() {
        Set<RedisReceptor> receptors = new LinkedHashSet<>();
        for (Receptor receptor : super.getAllReceptors()) {
            receptors.add((RedisReceptor) receptor);
        }
        return receptors.toArray(new RedisReceptor[0]);
    }
    public RedisTable[] getTables() {
        Set<RedisTable> tables = new LinkedHashSet<>();
        for (Table table : DataAPI.TABLES.get(this)) {
            tables.add((RedisTable) table);
        }
        return tables.toArray(new RedisTable[0]);
    }
    @Override
    public @NotNull RedisVariable[] getVariables() {
        Set<RedisVariable> receptors = new LinkedHashSet<>();
        for (Variable variable : DataAPI.VARIABLES.get(this)) {
            receptors.add((RedisVariable) variable);
        }
        return receptors.toArray(new RedisVariable[0]);
    }
    @Override
    public @NotNull RedisReceptor[] getReceptors() {
        Set<RedisReceptor> receptors = new LinkedHashSet<>();
        for (Receptor receptor : DataAPI.RECEPTORS.get(this)) {
            receptors.add((RedisReceptor) receptor);
        }
        return receptors.toArray(new RedisReceptor[0]);
    }

    @Override
    public @NotNull RedisLettuceDatabaseType getDatabaseType() {
        return (RedisLettuceDatabaseType) super.getDatabaseType();
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
