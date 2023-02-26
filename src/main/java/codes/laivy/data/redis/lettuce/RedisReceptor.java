package codes.laivy.data.redis.lettuce;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.redis.lettuce.variables.RedisActiveVariable;
import codes.laivy.data.redis.lettuce.variables.RedisInactiveVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    }

    @Override
    public void load() {
        super.load();

        RedisDatabase.RECEPTORS.putIfAbsent(getDatabase(), new LinkedHashSet<>());
        RedisDatabase.RECEPTORS.get(getDatabase()).add(this);

        if (getTable() != null) {
            RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).add(this);
        }

        loaded = true;
        getDatabase().getDatabaseType().receptorLoad(this);
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);

        RedisDatabase.RECEPTORS.get(getDatabase()).remove(this);
        if (getTable() != null) {
            RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).remove(this);
        }
    }

    public @Nullable RedisTable getTable() {
        return table;
    }

    @Override
    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    @ApiStatus.Experimental
    public @NotNull Set<@NotNull String> getRedisKeys() {
        String pattern;
        if (getTable() != null) {
            pattern = "DataAPI:" + getDatabase().getName() + "_" + getTable().getName() + "_*_" + getBruteId();
        } else {
            pattern = "DataAPI:" + getDatabase().getName() + "_*_" + getBruteId();
        }

        return new LinkedHashSet<>(getDatabase().getDatabaseType().getConnection().sync().keys(pattern));
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
    public void delete() {
        super.delete();
        getDatabase().getDatabaseType().receptorDelete(this);
        RedisDatabase.RECEPTORS.get(getDatabase()).remove(this);
        if (getTable() != null) {
            RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).remove(this);
        }
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
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        getDatabase().getDatabaseType().receptorSave(this);
    }
}
