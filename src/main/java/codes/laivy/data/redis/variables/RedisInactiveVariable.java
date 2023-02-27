package codes.laivy.data.redis.variables;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.redis.receptor.RedisReceptor;
import codes.laivy.data.redis.RedisVariable;
import org.jetbrains.annotations.NotNull;

public class RedisInactiveVariable extends InactiveVariable {
    public RedisInactiveVariable(@NotNull RedisReceptor receptor, @NotNull String variableName, @NotNull String value) {
        super(receptor, variableName, value);
    }

    @Override
    protected boolean load() {
        RedisVariable variable;
        if ((variable = (RedisVariable) DataAPI.getVariable(getReceptor().getDatabase(), getVariable())) != null) {
            new RedisActiveVariable(variable, getReceptor(), (variable.isSerialize() ? Variable.unserialize(getValue()) : getValue()));
            return false;
        }
        return true;
    }

    @Override
    public @NotNull RedisReceptor getReceptor() {
        return (RedisReceptor) super.getReceptor();
    }
}
