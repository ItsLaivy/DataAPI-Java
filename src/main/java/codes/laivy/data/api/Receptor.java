package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.api.variables.VariableValue;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Receptor {

    private final Table table;
    private String name;
    private final String bruteId;

    public Receptor(@NotNull Table table, @NotNull String name, @NotNull String bruteId) {
        this.table = table;
        this.name = name;
        this.bruteId = bruteId;

        if (DataAPI.getReceptor(table, bruteId) != null) {
            throw new IllegalStateException("A receptor with brute id '" + bruteId + "' at table '" + table.getName() + "' of the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')' already exists");
        }

        DataAPI.ACTIVE_VARIABLES.put(this, new ArrayList<>());
        DataAPI.INACTIVE_VARIABLES.put(this, new ArrayList<>());

        getTable().getDatabase().getDatabaseType().receptorLoad(this);

        DataAPI.RECEPTORS.get(table).add(this);
    }

    @NotNull
    public Table getTable() {
        return table;
    }
    @NotNull
    public String getBruteId() {
        return bruteId;
    }

    @NotNull
    public String getName() {
        return name;
    }
    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void unload(boolean save) {
        if (save) save();

        DataAPI.RECEPTORS.get(table).remove(this);
        DataAPI.ACTIVE_VARIABLES.remove(this);
        DataAPI.INACTIVE_VARIABLES.remove(this);
    }

    public void delete() {
        unload(false);
        getTable().getDatabase().getDatabaseType().receptorDelete(this);
    }

    public void save() {
        getTable().getDatabase().getDatabaseType().save(this);
    }

    public <T> T get(@NotNull String name) {
        //noinspection unchecked
        return (T) new VariableValue<>(getActiveVariable(name)).getValue();
    }
    public void set(@NotNull String name, Serializable value) {
        new VariableValue<>(getActiveVariable(name)).setValue(value);
    }

    public InactiveVariable[] getInactiveVariables() {
        InactiveVariable[] inactiveVariables = new InactiveVariable[DataAPI.INACTIVE_VARIABLES.get(this).size()];
        int row = 0;
        for (InactiveVariable inactiveVariable : DataAPI.INACTIVE_VARIABLES.get(this)) {
            inactiveVariables[row] = inactiveVariable;
            row++;
        }
        return inactiveVariables;
    }
    public ActiveVariable[] getActiveVariables() {
        ActiveVariable[] activeVariables = new ActiveVariable[DataAPI.ACTIVE_VARIABLES.get(this).size()];
        int row = 0;
        for (ActiveVariable inactiveVariable : DataAPI.ACTIVE_VARIABLES.get(this)) {
            activeVariables[row] = inactiveVariable;
            row++;
        }
        return activeVariables;
    }

    /**
     * @throws NullPointerException if a variable with that name doesn't exist
     */
    public InactiveVariable getInactiveVariable(String name) {
        InactiveVariable var = DataAPI.getInactiveVariable(this, name);

        if (var == null) {
            throw new NullPointerException("This receptor doesn't contains any inactive variable named '" + name + "' at the table '" + getTable().getName() + "' of the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')'");
        }

        return var;
    }
    /**
     * @throws NullPointerException if a variable with that name doesn't exist
     */
    public ActiveVariable getActiveVariable(String name) {
        ActiveVariable var = DataAPI.getActiveVariable(this, name);

        if (var == null) {
            throw new NullPointerException("This receptor doesn't contains any active variable named '" + name + "' at the table '" + getTable().getName() + "' of the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')'");
        }

        return var;
    }

}
