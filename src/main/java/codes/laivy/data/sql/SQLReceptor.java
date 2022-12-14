package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Receptor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

public class SQLReceptor extends Receptor {

    public static @NotNull SQLReceptor getCreateReceptor(@NotNull SQLTable SQLTable, @NotNull String name, @NotNull String bruteId) {
        SQLReceptor receptor;
        if ((receptor = DataAPI.getSQLReceptor(SQLTable, bruteId)) != null) {
            return receptor;
        } else {
            return new SQLReceptor(SQLTable, name, bruteId);
        }
    }

    // ---/-/--- //

    private final @NotNull SQLTable sqlTable;

    public SQLReceptor(@NotNull SQLTable sqlTable, @NotNull String name, @NotNull String bruteId) {
        super(sqlTable.getDatabase(), name, bruteId);
        this.sqlTable = sqlTable;

        if (DataAPI.getSQLReceptor(sqlTable, bruteId) != null) {
            throw new IllegalStateException("A SQLReceptor with that properties already exists!");
        }

        getDatabase().getDatabaseType().receptorLoad(this);

        SQLTable.SQL_RECEPTORS.get(sqlTable).add(this);
    }

    public @NotNull SQLTable getTable() {
        return sqlTable;
    }

    @Override
    public @NotNull SQLDatabase getDatabase() {
        return (SQLDatabase) super.getDatabase();
    }

    @Override
    public void unload(boolean save) {
        super.unload(save);
        SQLTable.SQL_RECEPTORS.get(getTable()).remove(this);
    }

    @Override
    public void delete() {
        unload(false);
        getDatabase().getDatabaseType().receptorDelete(this);
    }

    @Override
    public void save() {
        getDatabase().getDatabaseType().save(this);
    }

    @Override
    public void reload() {
        DataAPI.ACTIVE_VARIABLES.put(this, new LinkedHashSet<>());
        DataAPI.INACTIVE_VARIABLES.put(this, new LinkedHashSet<>());

        getDatabase().getDatabaseType().receptorLoad(this);
    }
}
