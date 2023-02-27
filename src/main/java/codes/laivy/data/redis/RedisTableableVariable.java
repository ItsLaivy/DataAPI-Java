package codes.laivy.data.redis;

import codes.laivy.data.api.table.Tableable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RedisTableableVariable extends RedisVariable implements Tableable {

    private final @NotNull RedisTable table;

    public RedisTableableVariable(@NotNull RedisTable table, @NotNull String name, @Nullable Object defaultValue) {
        this(table, name, defaultValue, true);
    }
    public RedisTableableVariable(@NotNull RedisTable table, @NotNull String name, @Nullable Object defaultValue, boolean serialize) {
        super(table.getDatabase(), name, defaultValue, serialize);
        this.table = table;

        RedisTable.REDIS_TABLED_VARIABLES.get(table).add(this);
    }

    @Override
    public @NotNull RedisTable getTable() {
        return table;
    }

    public void delete() {
        super.delete();
        RedisTable.REDIS_TABLED_VARIABLES.get(table).remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisTableableVariable)) return false;
        RedisTableableVariable that = (RedisTableableVariable) o;
        return getDatabase().equals(that.getDatabase()) && getName().equals(that.getName()) && getTable().equals(that.getTable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatabase(), getName(), getTable());
    }
}
