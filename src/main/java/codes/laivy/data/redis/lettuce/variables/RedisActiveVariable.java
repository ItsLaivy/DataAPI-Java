package codes.laivy.data.redis.lettuce.variables;

import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.redis.lettuce.RedisReceptor;
import codes.laivy.data.redis.lettuce.RedisVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedisActiveVariable extends ActiveVariable {
    public RedisActiveVariable(@NotNull RedisVariable variable, @NotNull RedisReceptor receptor, @Nullable Object value) {
        super(variable, receptor, value);
    }

    @Override
    public @NotNull RedisVariable getVariable() {
        return (RedisVariable) super.getVariable();
    }

    @Override
    public @NotNull RedisReceptor getReceptor() {
        return (RedisReceptor) super.getReceptor();
    }

    public @NotNull String getRedisVariableName() {
        return getVariable().getRedisVariableName(getReceptor());
    }
}
