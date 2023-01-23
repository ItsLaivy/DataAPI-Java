package codes.laivy.data.redis.lettuce;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.redis.lettuce.variables.RedisActiveVariable;
import codes.laivy.data.redis.lettuce.variables.RedisInactiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RedisReceptor extends Receptor {

    public static @NotNull RedisReceptor getCreateReceptor(@NotNull RedisDatabase database, @NotNull String name, @NotNull String bruteId, @Nullable RedisTable table) {
        RedisReceptor receptor;
        if ((receptor = DataAPI.getRedisReceptor(database, bruteId, table)) != null) {
            return receptor;
        } else {
            return new RedisReceptor(database, name, bruteId, table);
        }
    }

    // ---/-/--- //

    private final @NotNull Set<@NotNull String> redisKeys = new HashSet<>();

    private final @Nullable RedisTable table;

    public RedisReceptor(@NotNull RedisDatabase database, @NotNull String name, @NotNull String bruteId) {
        this(database, name, bruteId, null);
    }
    public RedisReceptor(@NotNull RedisDatabase database, @NotNull String name, @NotNull String bruteId, @Nullable RedisTable table) {
        super(database, name, bruteId);

        this.table = table;
        if (getTable() != null && !getTable().getDatabase().equals(database)) {
            throw new IllegalArgumentException("This table's database needs to be the same of the receptor's database!");
        }

        if (DataAPI.getRedisReceptor(database, bruteId, table) != null) {
            throw new IllegalStateException("A RedisReceptor with that properties already exists!");
        }

        load();

        RedisDatabase.RECEPTORS.putIfAbsent(database, new LinkedHashSet<>());
        RedisDatabase.RECEPTORS.get(database).add(this);

        if (getTable() != null) {
            RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).add(this);
        }
    }

    @Override
    public void load() {
        if (isLoaded()) {
            throw new IllegalStateException("Receptor already loaded");
        }

        loaded = true;
        getDatabase().getDatabaseType().receptorLoad(this);
    }

    public @Nullable RedisTable getTable() {
        return table;
    }

    @Override
    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    public @NotNull Set<@NotNull String> getRedisKeys() {
        return redisKeys;
    }
    @Override
    public @NotNull RedisActiveVariable getActiveVariable(@NotNull String name) {
        return (RedisActiveVariable) super.getActiveVariable(name);
    }

    @Override
    public @NotNull RedisActiveVariable[] getActiveVariables() {
        ActiveVariable[] dVars = super.getActiveVariables();
        RedisActiveVariable[] variables = new RedisActiveVariable[dVars.length];

        for (int row = 0; row < dVars.length; row++) {
            variables[row] = (RedisActiveVariable) dVars[row];
        }

        return variables;
    }
    @Override
    public @NotNull RedisInactiveVariable getInactiveVariable(@NotNull String name) {
        return (RedisInactiveVariable) super.getInactiveVariable(name);
    }

    @Override
    public @NotNull RedisInactiveVariable[] getInactiveVariables() {
        InactiveVariable[] dVars = super.getInactiveVariables();
        RedisInactiveVariable[] variables = new RedisInactiveVariable[dVars.length];

        for (int row = 0; row < dVars.length; row++) {
            variables[row] = (RedisInactiveVariable) dVars[row];
        }

        return variables;
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);
        RedisDatabase.RECEPTORS.get(getDatabase()).remove(this);
        if (getTable() != null) {
            RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).remove(this);
        }
    }

    @Override
    public void delete() {
        unload(false);
        getDatabase().getDatabaseType().receptorDelete(this);
    }

    @Override
    public void reload() {
        DataAPI.ACTIVE_VARIABLES.put(this, new LinkedHashSet<>());
        DataAPI.INACTIVE_VARIABLES.put(this, new LinkedHashSet<>());

        getDatabase().getDatabaseType().receptorLoad(this);
    }

    @Override
    public void save() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor isn't loaded.");
        }

        getDatabase().getDatabaseType().save(this);
    }
}
