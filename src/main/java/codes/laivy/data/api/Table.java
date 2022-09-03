package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Table {

    private final Database database;
    private final String name;

    public Table(@NotNull Database database, @NotNull String name) {
        this.database = database;
        this.name = name;

        if (DataAPI.getTable(database, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A table named '" + name + "' from database '" + database.getName() + " ('" + database.getDatabaseType().getName() + "')' already exists!");
            return;
        }

        database.getDatabaseType().tableLoad(this);

        DataAPI.TABLES.get(database).add(this);
        DataAPI.RECEPTORS.put(this, new ArrayList<>());
        DataAPI.VARIABLES.put(this, new ArrayList<>());
    }

    @NotNull
    public Database getDatabase() {
        return database;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void delete() {
        // Unload receptors
        for (Receptor receptor : getReceptors()) {
            receptor.unload(false);
        }
        // Unload variables
        for (Variable variable : getVariables()) {
            variable.delete();
        }

        getDatabase().getDatabaseType().tableDelete(this);
    }

    public Receptor[] getReceptors() {
        Receptor[] receptors = new Receptor[DataAPI.RECEPTORS.get(this).size()];
        int row = 0;
        for (Receptor receptor : DataAPI.RECEPTORS.get(this)) {
            receptors[row] = receptor;
            row++;
        }
        return receptors;
    }

    public Variable[] getVariables() {
        Variable[] variables = new Variable[DataAPI.VARIABLES.get(this).size()];
        int row = 0;
        for (Variable variable : DataAPI.VARIABLES.get(this)) {
            variables[row] = variable;
            row++;
        }
        return variables;
    }

}
