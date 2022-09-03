package codes.laivy.data.api.variables;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;

public class InactiveVariable {

    private final Receptor receptor;
    private final String variable;
    private final String value;

    public InactiveVariable(@NotNull Receptor receptor, @NotNull String variableName, @NotNull String value) {
        this.variable = variableName;
        this.receptor = receptor;
        this.value = value;

        Variable variable = DataAPI.getVariable(receptor.getTable(), variableName);
        if (variable != null) {
            new ActiveVariable(variable, receptor, Variable.unserialize(value));
            return;
        }

        DataAPI.INACTIVE_VARIABLES.get(receptor).add(this);
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