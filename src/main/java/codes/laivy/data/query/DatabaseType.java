package codes.laivy.data.query;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;

public abstract class DatabaseType<R extends Receptor, V extends Variable> {

    @NotNull
    public abstract String[] suppressedErrors();

    protected final @NotNull String name;

    public DatabaseType(@NotNull String name) {
        this.name = name;
        DataAPI.DATABASES.putIfAbsent(this, new HashSet<>());
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void throwError(Throwable throwable) throws RuntimeException {
        if (DataAPI.DEBUG) System.out.println("Possible code error: '" + throwable.getClass() + "' - '" + throwable.getMessage() + "'");

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
    public abstract Map<String, Object> receptorData(@NotNull R receptor);

    /**
     * It's called when a receptor needs to be loaded
     */
    public abstract void receptorLoad(@NotNull R receptor);
    /**
     * It's called when a receptor needs to be deleted
     * <p>
     * Note: The {@link Receptor#unload(boolean)} is called first, and all variables
     * or (in)active variables already has get deleted.
     */
    public abstract void receptorDelete(@NotNull R receptor);

    /**
     * It's used when a table needs get saved
     */
    public abstract void receptorSave(@NotNull R receptor);

    /**
     * Gets all receptors instances stored at the database (doesn't loads it)
     */
    public abstract @NotNull Receptor[] receptorList();

    // Variables

    /**
     * It's used when a variable get loaded
     */
    public abstract void variableLoad(@NotNull V variable);
    /**
     * It's used when a variable get deleted
     */
    public abstract void variableDelete(@NotNull V variable);

    // Databases

    /**
     * It's used to create a new database
     */
    public abstract void databaseLoad(@NotNull Database database);
    /**
     * It's used to delete a database
     */
    public abstract void databaseDelete(@NotNull Database database);

    /**
     * @return all databases of this type
     */
    public abstract @NotNull Database[] getDatabases();
}
