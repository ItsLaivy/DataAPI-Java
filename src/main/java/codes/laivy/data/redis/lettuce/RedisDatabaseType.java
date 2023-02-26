package codes.laivy.data.redis.lettuce;

import codes.laivy.data.api.Database;
import codes.laivy.data.api.Variable;
import codes.laivy.data.query.DatabaseType;
import codes.laivy.data.redis.lettuce.variables.RedisActiveVariable;
import codes.laivy.data.redis.lettuce.variables.RedisInactiveVariable;
import codes.laivy.data.utils.ObjectsUtils;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisDatabaseType extends DatabaseType<RedisReceptor, RedisVariable> {

    private final @NotNull RedisClient client;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    private final @NotNull String host;
    private final int port;

    public RedisDatabaseType(@NotNull String host, int port) {
        this(host, null, 15000, port, false);
    }
    public RedisDatabaseType(@NotNull String host, @Nullable String password, int timeoutMillis, int port, boolean ssl) {
        super("REDIS");
        this.host = host;
        this.port = port;

        // RedisClient creator
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withSsl(ssl)
                .withTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        if (password != null) builder.withPassword(password);

        this.client = RedisClient.create(builder.build());
        this.connection = this.client.connect();
        //
    }

    public @NotNull RedisClient getClient() {
        return client;
    }
    public @NotNull StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }

    public @NotNull String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public @NotNull String[] suppressedErrors() {
        return new String[0];
    }

    @Override
    public @NotNull Map<String, String> data(@NotNull RedisReceptor receptor) {
        return new HashMap<>();
    }

    public boolean isKeyRegisteredAtRedis(@NotNull String key) {
        return getConnection().sync().exists(key);
    }

    @Override
    public void receptorLoad(@NotNull RedisReceptor receptor) {
        receptor.setNew(false);

        for (RedisVariable variable : receptor.getDatabase().getVariables()) {
            if (ObjectsUtils.equals(variable.getTable(), receptor.getTable())) {
                String key = variable.getRedisVariableName(receptor);
                if (isKeyRegisteredAtRedis(key)) {
                    receptor.setNew(false);
                    new RedisInactiveVariable(receptor, variable.getName(), getConnection().sync().get(key));
                } else {
                    new RedisActiveVariable(variable, receptor, variable.getDefaultValue());
                }
            }
        }

        receptorSave(receptor);
    }

    @Override
    public void receptorDelete(@NotNull RedisReceptor receptor) {
        for (String key : receptor.getRedisKeys()) {
            getConnection().sync().del(key);
        }
    }

    @Override
    public void receptorSave(@NotNull RedisReceptor receptor) {
        for (RedisActiveVariable variable : receptor.getActiveVariables()) {
            String data;
            if (variable.getVariable().isSerialize()) {
                if (variable.getValue() != null && !(variable.getValue() instanceof Serializable)) {
                    throw new IllegalArgumentException("The variable is a serializable variable, but the current value isn't a instance of Serializable!");
                }

                data = Variable.serialize((Serializable) variable.getValue());
            } else {
                data = variable.getValue() != null ? variable.getValue().toString() : "";
            }

            getConnection().sync().set(variable.getRedisVariableName(), data);
        }
    }

    @Override
    public void variableLoad(@NotNull RedisVariable variable) {

    }

    @Override
    public void variableDelete(@NotNull RedisVariable variable) {

    }

    @Override
    public void databaseLoad(@NotNull Database database) {

    }

    @Override
    public void databaseDelete(@NotNull Database database) {

    }
}
