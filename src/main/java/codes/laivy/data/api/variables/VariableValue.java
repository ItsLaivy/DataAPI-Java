package codes.laivy.data.api.variables;

import codes.laivy.data.api.Receptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class VariableValue<T> {

    protected final Receptor receptor;
    protected final ActiveVariable variable;

    public VariableValue(@NotNull Receptor receptor, @NotNull String name) {
        this(receptor.getActiveVariable(name));
    }
    public VariableValue(@NotNull ActiveVariable variable) {
        this.receptor = variable.getReceptor();
        this.variable = variable;
    }

    @Nullable
    public T getValue() {
        //noinspection unchecked
        return (T) this.variable.getValue();
    }
    public void setValue(@Nullable T value) {
        this.variable.setValue(value);
    }

    public void addValue(@NotNull Byte value) {
        if (getValue() instanceof Byte) {
            variable.setValue((Byte) getValue() + value);
        }
    }
    public void addValue(@NotNull Double value) {
        if (getValue() instanceof Double) {
            variable.setValue((Double) getValue() + value);
        }
    }
    public void addValue(@NotNull Float value) {
        if (getValue() instanceof Float) {
            variable.setValue((Float) getValue() + value);
        }
    }
    public void addValue(@NotNull Integer value) {
        if (getValue() instanceof Integer) {
            variable.setValue((Integer) getValue() + value);
        }
    }
    public void addValue(@NotNull Long value) {
        if (getValue() instanceof Long) {
            variable.setValue((Long) getValue() + value);
        }
    }
    public void addValue(@NotNull Short value) {
        if (getValue() instanceof Short) {
            variable.setValue((Short) getValue() + value);
        }
    }

    @NotNull
    public Receptor getReceptor() {
        return receptor;
    }

    @NotNull
    public ActiveVariable getVariable() {
        return variable;
    }

}
