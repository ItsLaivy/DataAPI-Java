package codes.laivy.data.sql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.Variable;
import codes.laivy.data.api.variables.ActiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class SQLVariable extends Variable {

    /**
     * That's the size (in bytes) a variable can store.
     * Don't use unnecessary sizes, if you are convinced you will use only some bytes, you don't need the {@link #BIG}.
     * With more bytes, more performance issues.
     */
    public enum Size {

        /**
         * Recommended for non-serializable values, can store up to 255 bytes.
         */
        TINY(255L),

        /**
         * Recommended for simple serializable values, can store up to 65.535 bytes (~64KB).
         */
        NORMAL(65535L),

        /**
         * Recommended for big serializable values, can store up to 16.777.215 bytes (~16MB).
         * That's the default one.
         */
        MEDIUM(16777215L),

        /**
         * Recommended for massive values, can store up to 4.294.967.295 bytes (~4GB).
         * Only use that if you are absolutely convinced that you will need store a large amount of that,
         * this can cause performance issues.
         */
        BIG(4294967295L),
        ;

        private final @Range(from = 1, to = Long.MAX_VALUE) long limit;

        Size(@Range(from = 1, to = Long.MAX_VALUE) long limit) {
            this.limit = limit;
        }

        public @Range(from = 1, to = Long.MAX_VALUE) long getLimit() {
            return limit;
        }
    }

    private final @NotNull SQLTable sqlTable;
    private final @NotNull Size size;

    public SQLVariable(@NotNull String name, @NotNull SQLTable sqlTable, @Nullable Object defaultValue) {
        this(name, sqlTable, defaultValue, true, true);
    }

    public SQLVariable(@NotNull String name, @NotNull SQLTable sqlTable, @Nullable Object defaultValue, boolean serialize, boolean saveToDatabase) {
        this(Size.MEDIUM, name, sqlTable, defaultValue, serialize, saveToDatabase);
    }
    public SQLVariable(@NotNull Size size, @NotNull String name, @NotNull SQLTable sqlTable, @Nullable Object defaultValue, boolean serialize, boolean saveToDatabase) {
        super(name, sqlTable.getDatabase(), defaultValue, serialize, saveToDatabase);
        this.sqlTable = sqlTable;
        this.size = size;

        if (DataAPI.getSQLVariable(sqlTable, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A variable named '" + name + "' at the table '" + getTable().getName() + "' in the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')' already exists!");
            return;
        }

        getDatabase().getDatabaseType().variableLoad(this);

        SQLTable.SQL_VARIABLES.get(sqlTable).add(this);

        for (SQLReceptor receptor : SQLTable.SQL_RECEPTORS.get(sqlTable)) {
            new ActiveVariable(this, receptor, defaultValue);
        }
    }

    @Override
    public @NotNull SQLDatabase getDatabase() {
        return (SQLDatabase) super.getDatabase();
    }

    public @NotNull Size getSize() {
        return size;
    }

    public void delete() {
        for (SQLReceptor receptor : SQLTable.SQL_RECEPTORS.get(getTable())) {
            DataAPI.ACTIVE_VARIABLES.get(receptor).removeIf(activeVariable -> activeVariable.getVariable().equals(this));
            DataAPI.INACTIVE_VARIABLES.get(receptor).removeIf(inactiveVariable -> inactiveVariable.getVariable().equals(this.getName()));
        }

        SQLTable.SQL_VARIABLES.get(getTable()).remove(this);
        getTable().getDatabase().getDatabaseType().variableDelete(this);
    }

    public @NotNull SQLTable getTable() {
        return sqlTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLVariable)) return false;
        SQLVariable that = (SQLVariable) o;
        return sqlTable.equals(that.sqlTable) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlTable, getName());
    }
}
