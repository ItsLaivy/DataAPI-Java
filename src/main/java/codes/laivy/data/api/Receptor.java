package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import codes.laivy.data.api.variables.VariableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public abstract class Receptor {

    protected boolean isNew = false;

    protected @NotNull String name;
    protected final @NotNull String bruteId;
    protected final @NotNull Database database;

    public abstract void save();
    public abstract void delete();

    public abstract void reload();

    public Receptor(@NotNull Database database, @NotNull String name, @NotNull String bruteId) {
        this.database = database;
        this.name = name;
        this.bruteId = bruteId;

        DataAPI.ACTIVE_VARIABLES.putIfAbsent(this, new HashSet<>());
        DataAPI.INACTIVE_VARIABLES.putIfAbsent(this, new HashSet<>());

        DataAPI.RECEPTORS.putIfAbsent(database, new LinkedHashSet<>());
        DataAPI.RECEPTORS.get(database).add(this);
    }

    public boolean isNew() {
        return isNew;
    }
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public @NotNull String getBruteId() {
        return bruteId;
    }

    public @NotNull String getName() {
        return name;
    }
    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public void unload(boolean save) {
        if (save) save();

        DataAPI.RECEPTORS.get(database).remove(this);
        DataAPI.ACTIVE_VARIABLES.remove(this);
        DataAPI.INACTIVE_VARIABLES.remove(this);
    }

    public @NotNull <T> T get(@NotNull String name) {
        //noinspection unchecked
        return (T) Objects.requireNonNull(new VariableValue<>(getActiveVariable(name)).getValue());
    }
    public void set(@NotNull String name, @Nullable Object value) {
        new VariableValue<>(getActiveVariable(name)).setValue(value);
    }

    public @NotNull InactiveVariable[] getInactiveVariables() {
        InactiveVariable[] inactiveVariables = new InactiveVariable[DataAPI.INACTIVE_VARIABLES.get(this).size()];
        int row = 0;
        for (InactiveVariable inactiveVariable : DataAPI.INACTIVE_VARIABLES.get(this)) {
            inactiveVariables[row] = inactiveVariable;
            row++;
        }
        return inactiveVariables;
    }
    public @NotNull ActiveVariable[] getActiveVariables() {
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
    public @NotNull InactiveVariable getInactiveVariable(@NotNull String name) {
        InactiveVariable var = DataAPI.getInactiveVariable(this, name);

        if (var == null) {
            throw new NullPointerException("This receptor doesn't contains any inactive variable named '" + name + "' of the database '" + getDatabase().getName() + " ('" + getDatabase().getDatabaseType().getName() + "')'");
        }

        return var;
    }
    /**
     * @throws NullPointerException if a variable with that name doesn't exist
     */
    public @NotNull ActiveVariable getActiveVariable(@NotNull String name) {
        ActiveVariable var = DataAPI.getActiveVariable(this, name);

        if (var == null) {
            throw new NullPointerException("This receptor doesn't contains any active variable named '" + name + "' of the database '" + getDatabase().getName() + " ('" + getDatabase().getDatabaseType().getName() + "')'");
        }

        return var;
    }
}
