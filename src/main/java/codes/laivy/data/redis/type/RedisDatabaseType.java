package codes.laivy.data.redis.type;

import codes.laivy.data.query.DatabaseType;
import codes.laivy.data.redis.RedisDatabase;
import codes.laivy.data.redis.RedisTable;
import codes.laivy.data.redis.RedisVariable;
import codes.laivy.data.redis.receptor.RedisReceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class RedisDatabaseType extends DatabaseType<RedisReceptor, RedisVariable> {

    private final @NotNull String host;
    private final @Nullable String password;
    private final @Range(from = 0, to = 65535) int port;

    public RedisDatabaseType(@NotNull String name, @NotNull String host, @Nullable String password, @Range(from = 0, to = 65535) int port) {
        super(name);
        this.host = host;
        this.password = password;
        this.port = port;
    }

    public @NotNull String getHost() {
        return host;
    }

    public @Nullable String getPassword() {
        return password;
    }

    public @Range(from = 0, to = 65535) int getPort() {
        return port;
    }

    public abstract @NotNull RedisDatabase[] getDatabases();

    public @NotNull RedisTable[] getTables() {
        Set<RedisTable> tables = new LinkedHashSet<>();
        for (RedisDatabase database : getDatabases()) {
            tables.addAll(Arrays.asList(database.getTables()));
        }
        return tables.toArray(new RedisTable[0]);
    }
}
