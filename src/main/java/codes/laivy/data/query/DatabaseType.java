package codes.laivy.data.query;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Database;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.Table;
import codes.laivy.data.api.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public abstract class DatabaseType {

    @NotNull
    public abstract String[] suppressedErrors();

    private final String name;

    public DatabaseType(@NotNull String name) {
        this.name = name;

        if (!DataAPI.DATABASES.containsKey(this)) {
            DataAPI.DATABASES.put(this, new ArrayList<>());
        }
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void throwError(Throwable throwable) {
        if (DataAPI.DEBUG) System.out.println("Possible code error: '" + throwable.getClass() + "' - '" + throwable.getMessage() + "'");;

        for (String str : suppressedErrors()) {
            if (throwable.getMessage().contains(str)) {
                return;
            }
        }

        throw new RuntimeException(throwable);
    }

    // Receptors

    /**
     * It's used to get receptor's database values
     * @return this Map needs to return a Map. (key = variable name) e (value = serializaded value)
     */
    @NotNull
    public abstract Map<String, String> data(@NotNull Receptor receptor);

    /**
     * It's called when a receptor needs to be loaded
     */
    public abstract void receptorLoad(@NotNull Receptor receptor);
    /**
     * It's called when a receptor needs to be deleted
     *
     * Note: The {@link Receptor#unload(boolean)} is called first, and all variables
     * or (in)active variables already has get deleted.
     */
    public abstract void receptorDelete(@NotNull Receptor receptor);

    /**
     * It's used when a table needs get saved
     */
    public abstract void save(@NotNull Receptor receptor);

    // Tables

    /**
     * It's used when a table needs to load
     */
    public abstract void tableLoad(@NotNull Table table);
    /**
     * It's used when a table get deleted
     */
    public abstract void tableDelete(@NotNull Table table);

    // Variables

    /**
     * It's used when a variable get loaded
     */
    public abstract void variableLoad(@NotNull Variable variable);
    /**
     * It's used when a variable get deleted
     */
    public abstract void variableDelete(@NotNull Variable variable);

    // Databases

    /**
     * It's used to create a new database
     */
    public abstract void databaseLoad(@NotNull Database database);
    /**
     * It's used to delete a database
     */
    public abstract void databaseDelete(@NotNull Database database);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseType that = (DatabaseType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
