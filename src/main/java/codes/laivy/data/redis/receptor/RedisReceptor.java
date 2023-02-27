package codes.laivy.data.redis.receptor;

import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.table.Tableable;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.redis.RedisVariable;
import codes.laivy.data.redis.RedisDatabase;
import codes.laivy.data.redis.variables.RedisActiveVariable;
import codes.laivy.data.redis.variables.RedisInactiveVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class RedisReceptor extends Receptor {

    public RedisReceptor(@NotNull RedisDatabase database, @NotNull String name, @NotNull String bruteId) {
        super(database, name, bruteId);
    }

    /**
     * Returns all the redis variables that matches with that receptor
     * @return the redis variables of that receptor
     */
    public @NotNull RedisVariable[] getVariables() {
        Set<RedisVariable> variables = new LinkedHashSet<>();
        for (RedisVariable variable : RedisDatabase.VARIABLES.get(getDatabase())) {
            if (variable instanceof Tableable) {
                continue;
            }
            variables.add(variable);
        }
        return variables.toArray(new RedisVariable[0]);
    }

    @Override
    public void load() {
        super.load();

        RedisDatabase.RECEPTORS.putIfAbsent(getDatabase(), new LinkedHashSet<>());
        RedisDatabase.RECEPTORS.get(getDatabase()).add(this);

        loaded = true;
        getDatabase().getDatabaseType().receptorLoad(this);
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);
        RedisDatabase.RECEPTORS.get(getDatabase()).remove(this);
    }

    @Override
    public @NotNull RedisDatabase getDatabase() {
        return (RedisDatabase) super.getDatabase();
    }

    @ApiStatus.Experimental
    public @NotNull Set<@NotNull String> getRedisKeys() {
        String pattern = "DataAPI:" + getDatabase().getName() + "_*_" + getBruteId();
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
    }

    @Override
    public void save() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        getDatabase().getDatabaseType().receptorSave(this);
    }
}
