package codes.laivy.data.sql.mysql;

import codes.laivy.data.query.DataResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLResult extends DataResult {

    private final @Nullable ResultSet result;

    public @Nullable ResultSet getResult() {
        return result;
    }

    public MySQLResult(@Nullable ResultSet result) {
        this.result = result;
    }

    @Override
    public int columns() {
        if (result != null) {
            try {
                return result.getMetaData().getColumnCount();
            } catch (SQLException e) {
                throw new RuntimeException("Cannot get columns number of a MySQLResult", e);
            }
        } else {
            return 0;
        }
    }

    @Contract()
    @Override
    public @NotNull Set<Map<String, Object>> results() {
        if (result != null) {
            try {
                if (result.isClosed()) {
                    throw new IllegalStateException("The result set is closed");
                }

                Set<Map<String, Object>> set = new LinkedHashSet<>();
                while (result.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    if (result.getObject(1) == null) {
                        continue;
                    }

                    for (int row = 1; row <= result.getMetaData().getColumnCount(); row++) {
                        map.put(result.getMetaData().getColumnName(row), result.getObject(row));
                    }

                    set.add(map);
                }
                return set;
            } catch (SQLException e) {
                if (e.getMessage().equalsIgnoreCase("Illegal operation on empty result set.")) {
                    return new HashSet<>();
                }
                throw new RuntimeException("Cannot get columns data of a MySQLResult", e);
            }
        }
        return new HashSet<>();
    }

    @Override
    public void close() {
        if (result != null) {
            try {
                if (!result.isClosed()) {
                    result.getStatement().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
