package codes.laivy.data.api.variables;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ActiveVariable {

    private final Receptor receptor;
    private final Variable variable;
    private Serializable value;

    public ActiveVariable(@NotNull Variable variable, @NotNull Receptor receptor, @Nullable Serializable value) {
        this.variable = variable;
        this.receptor = receptor;
        this.value = value;

        InactiveVariable inactiveVariable = DataAPI.getInactiveVariable(receptor, variable.getName());
        if (inactiveVariable != null) {
            this.value = Variable.unserialize(inactiveVariable.getValue());
            DataAPI.INACTIVE_VARIABLES.get(receptor).remove(inactiveVariable);
        }

        DataAPI.ACTIVE_VARIABLES.get(receptor).add(this);
    }

    @NotNull
    public Receptor getReceptor() {
        return receptor;
    }

    @NotNull
    public Variable getVariable() {
        return variable;
    }

    @NotNull
    public Serializable getValue() {
        return this.value;
    }

    public void setValue(@Nullable Serializable value) {
        this.value = value;
    }

}
