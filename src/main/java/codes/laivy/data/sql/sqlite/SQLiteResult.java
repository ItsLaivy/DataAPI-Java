package codes.laivy.data.sql.sqlite;

import codes.laivy.data.query.DataResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLiteResult extends DataResult {

    private final @Nullable ResultSet result;

    public @Nullable ResultSet getResult() {
        return result;
    }

    public SQLiteResult(@Nullable ResultSet result) {
        this.result = result;
    }

    @Override
    public int columns() {
        if (result != null) {
            try {
                return result.getMetaData().getColumnCount();
            } catch (SQLException e) {
                throw new RuntimeException("Cannot get columns number of a SQLiteDataResult", e);
            }
        } else {
            return 0;
        }
    }

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
                throw new RuntimeException("Cannot get results data of a SQLiteDataResult", e);
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
