package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import codes.laivy.data.api.table.Tableable;
import org.jetbrains.annotations.NotNull;

public class SQLReceptor extends Receptor implements Tableable {

    private final @NotNull SQLTable table;

    public SQLReceptor(@NotNull SQLTable table, @NotNull String name, @NotNull String bruteId) {
        super(table.getDatabase(), name, bruteId);
        this.table = table;

        if (DataAPI.getSQLReceptor(table, bruteId) != null) {
            throw new IllegalStateException("A SQLReceptor with that properties already exists!");
        }
    }

    @Override
    public void load() {
        super.load();

        SQLTable.SQL_RECEPTORS.get(table).add(this);

        loaded = true;
        getDatabase().getDatabaseType().receptorLoad(this);
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);
        SQLTable.SQL_RECEPTORS.get(getTable()).remove(this);
    }

    @Override
    public @NotNull SQLTable getTable() {
        return table;
    }

    @Override
    public @NotNull SQLDatabase getDatabase() {
        return (SQLDatabase) super.getDatabase();
    }

    @Override
    public void delete() {
        unload(false);
        getDatabase().getDatabaseType().receptorDelete(this);
    }

    @Override
    public void save() {
        if (!isLoaded()) {
            throw new IllegalStateException("This receptor '" + getBruteId() + "' isn't loaded.");
        }

        getDatabase().getDatabaseType().receptorSave(this);
    }
}
