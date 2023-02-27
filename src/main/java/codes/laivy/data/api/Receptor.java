package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.variables.ActiveVariable;
import codes.laivy.data.api.variables.InactiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class Receptor {

    protected boolean isNew = false;
    protected boolean loaded = false;

    protected @NotNull String name;
    protected final @NotNull String bruteId;
    protected final @NotNull Database database;

    public abstract void save();

    public Receptor(@NotNull Database database, @NotNull String name, @NotNull String bruteId) {
        this.database = database;
        this.name = name;
        this.bruteId = bruteId;

        DataAPI.ACTIVE_VARIABLES.putIfAbsent(this, new LinkedHashSet<>());
        DataAPI.INACTIVE_VARIABLES.putIfAbsent(this, new LinkedHashSet<>());
    }

    public void reload(boolean save) {
        if (isLoaded()) {
            unload(save);
        } load();
    }

    public boolean isNew() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    public @NotNull String getBruteId() {
        return bruteId;
    }

    public @NotNull String getName() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }
    public @NotNull Database getDatabase() {
        return database;
    }

    public void load() {
        if (isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' is already loaded");
        }

        DataAPI.RECEPTORS.putIfAbsent(getDatabase(), new LinkedHashSet<>());
        DataAPI.RECEPTORS.get(getDatabase()).add(this);

        DataAPI.ACTIVE_VARIABLES.put(this, new LinkedHashSet<>());
        DataAPI.INACTIVE_VARIABLES.put(this, new LinkedHashSet<>());
    }

    public void delete() {
        unload(false);
    }
    public void unload(boolean save) {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded");
        }

        save();

        DataAPI.RECEPTORS.get(getDatabase()).remove(this);
        DataAPI.ACTIVE_VARIABLES.get(this).clear();
        DataAPI.INACTIVE_VARIABLES.get(this).clear();;

        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public @Nullable <T> T get(@NotNull String name) {
        //noinspection unchecked
        return (T) getActiveVariable(name).getValue();
    }
    public void set(@NotNull String name, @Nullable Object value) {
        getActiveVariable(name).setValue(value);
    }

    public @NotNull InactiveVariable[] getInactiveVariables() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        InactiveVariable[] inactiveVariables = new InactiveVariable[DataAPI.INACTIVE_VARIABLES.get(this).size()];
        int row = 0;
        for (InactiveVariable inactiveVariable : DataAPI.INACTIVE_VARIABLES.get(this)) {
            inactiveVariables[row] = inactiveVariable;
            row++;
        }
        return inactiveVariables;
    }
    public @NotNull ActiveVariable[] getActiveVariables() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

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
