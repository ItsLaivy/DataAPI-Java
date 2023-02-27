package codes.laivy.data.redis.receptor;

import codes.laivy.data.api.table.Tableable;
import codes.laivy.data.redis.RedisDatabase;
import codes.laivy.data.redis.RedisTable;
import codes.laivy.data.redis.RedisVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class RedisTableableReceptor extends RedisReceptor implements Tableable {

    private final @NotNull RedisTable table;

    public RedisTableableReceptor(@NotNull RedisTable table, @NotNull String name, @NotNull String bruteId) {
        super(table.getDatabase(), name, bruteId);
        this.table = table;
    }

    public @NotNull RedisVariable[] getVariables() {
        Set<RedisVariable> variables = new LinkedHashSet<>();
        for (RedisVariable variable : RedisDatabase.VARIABLES.get(getDatabase())) {
            if (variable instanceof Tableable && ((Tableable) variable).getTable().equals(getTable())) {
                variables.add(variable);
            }
        }
        return variables.toArray(new RedisVariable[0]);
    }

    @Override
    public @NotNull RedisTable getTable() {
        return table;
    }

    @Override
    public void load() {
        super.load();
        RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).add(this);
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);
        RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).remove(this);
    }

    @ApiStatus.Experimental
    public @NotNull Set<@NotNull String> getRedisKeys() {
        String pattern = "DataAPI:" + getDatabase().getName() + "_" + getTable().getName() + "_*_" + getBruteId();
        return new LinkedHashSet<>(getDatabase().getDatabaseType().getConnection().sync().keys(pattern));
    }

    @Override
    public void delete() {
        super.delete();
        RedisTable.REDIS_TABLED_RECEPTORS.get(getTable()).remove(this);
    }
}
