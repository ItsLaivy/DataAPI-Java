package codes.laivy.data.redis.type.lettuce;

import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import codes.laivy.data.redis.RedisDatabase;
import codes.laivy.data.redis.RedisTable;
import codes.laivy.data.redis.RedisVariable;
import codes.laivy.data.redis.receptor.RedisReceptor;
import codes.laivy.data.redis.type.RedisDatabaseType;
import codes.laivy.data.redis.variables.RedisActiveVariable;
import codes.laivy.data.redis.variables.RedisInactiveVariable;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This is the Lettuce support for Redis, check the <a href="https://lettuce.io/">Lettuce.io</a> for more info and download
 * <br><br>
 * Tested versions:
 * <ul>
 *     <li>4.5.0.Final</li>
 * </ul>
 */
public class RedisLettuceDatabaseType extends RedisDatabaseType {

    private final @NotNull RedisClient client;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    public RedisLettuceDatabaseType(@NotNull String host, int port) {
        this(host, null, 16000, port, false);
    }
    public RedisLettuceDatabaseType(@NotNull String host, @NotNull String password, @Range(from = 0, to = 65535) int port) {
        this(host, password, 16000, port, false);
    }
    public RedisLettuceDatabaseType(@NotNull String host, @Nullable String password, @Range(from = 1, to = Integer.MAX_VALUE) int timeoutMillis, @Range(from = 0, to = 65535) int port, boolean ssl) {
        super("REDIS_LETTUCE", host, password, port);

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

    @Override
    public @NotNull RedisDatabase[] getDatabases() {
        return new RedisDatabase[0];
    }

    @Override
    public @NotNull String[] suppressedErrors() {
        return new String[0];
    }

    @Override
    public @NotNull Map<String, Object> receptorData(@NotNull RedisReceptor receptor) {
        return new HashMap<>();
    }

    public boolean isKeyRegisteredAtRedis(@NotNull String key) {
        return getConnection().sync().exists(key);
    }

    @Override
    public void receptorLoad(@NotNull RedisReceptor receptor) {
        receptor.setNew(false);

        for (RedisVariable variable : receptor.getVariables()) {
            String key =variable.getRedisVariableName(receptor);
            if (isKeyRegisteredAtRedis(key)) {
                receptor.setNew(false);
                new RedisInactiveVariable(receptor, variable.getName(), getConnection().sync().get(key));
            } else {
                new RedisActiveVariable(variable, receptor, variable.getDefaultValue());
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
            if (variable.getValue() != null) {
                if (variable.getVariable().isSerialize()) {
                    if (!(variable.getValue() instanceof Serializable)) {
                        throw new IllegalArgumentException("The variable is a serializable variable, but the current value isn't a instance of Serializable!");
                    }

                    data = Variable.serialize((Serializable) variable.getValue());
                } else {
                    data = variable.getValue().toString();
                }
            } else {
                data = null;
            }

            getConnection().sync().set(variable.getVariable().getRedisVariableName(receptor), data);
        }
    }

    @Override
    public @NotNull Receptor[] receptorList() {
        Set<Receptor> receptors = new LinkedHashSet<>();

        for (RedisDatabase database : getDatabases()) {
            // Keys from tables
            for (RedisTable table : database.getTables()) {
                String pattern = "dataapi:" + database.getName() + "_" + table.getName() + "_*_*";
                List<String> keys = getConnection().sync().keys(pattern);
            }
            // Keys from database (without tables)
            String pattern = "dataapi:" + database.getName() + "_*_*";
            List<String> keys = getConnection().sync().keys(pattern);
            //
        }

        return receptors.toArray(new Receptor[0]);
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
