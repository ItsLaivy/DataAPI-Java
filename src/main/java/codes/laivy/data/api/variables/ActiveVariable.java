package codes.laivy.data.api.variables;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ActiveVariable {

    protected final @NotNull Receptor receptor;
    protected final @NotNull Variable variable;
    protected @Nullable Object value;

    public ActiveVariable(@NotNull Variable variable, @NotNull Receptor receptor, @Nullable Object value) {
        this.variable = variable;
        this.receptor = receptor;
        this.value = value;

        if (load()) {
            DataAPI.ACTIVE_VARIABLES.get(receptor).add(this);
        }
    }

    protected boolean load() {
        if (Objects.equals(getValue(), "<!NULL>")) {
            this.value = null;
        }

        InactiveVariable inactiveVariable = DataAPI.getInactiveVariable(receptor, variable.getName());
        if (inactiveVariable != null) {
            this.value = Variable.unserialize(inactiveVariable.getValue());
            DataAPI.INACTIVE_VARIABLES.get(receptor).remove(inactiveVariable);
        }
        return true;
    }

    @NotNull
    public Receptor getReceptor() {
        return receptor;
    }

    @NotNull
    public Variable getVariable() {
        return variable;
    }

    @Nullable
    public Object getValue() {
        return this.value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

}
