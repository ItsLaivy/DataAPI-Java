package codes.laivy.data.api.variables;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;

public class InactiveVariable {

    protected final @NotNull Receptor receptor;
    protected final @NotNull String variable;
    protected final @NotNull String value;

    public InactiveVariable(@NotNull Receptor receptor, @NotNull String variableName, @NotNull String value) {
        this.variable = variableName;
        this.receptor = receptor;
        this.value = value;

        if (load()) {
            DataAPI.INACTIVE_VARIABLES.get(receptor).add(this);
        }
    }

    protected boolean load() {
        Variable variable;
        if ((variable = DataAPI.getVariable(getReceptor().getDatabase(), getVariable())) != null) {
            new ActiveVariable(variable, getReceptor(), (variable.isSerialize() ? Variable.unserialize(getValue()) : getValue()));
            return false;
        }
        return true;
    }


    @NotNull
    public Receptor getReceptor() {
        return receptor;
    }

    @NotNull
    public String getVariable() {
        return variable;
    }

    @NotNull
    public String getValue() {
        return value;
    }

}