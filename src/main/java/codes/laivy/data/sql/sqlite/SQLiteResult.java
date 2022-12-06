package codes.laivy.data.sql.sqlite;

import codes.laivy.data.query.DataResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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
    public @NotNull Map<String, String> results() {
        if (result != null) {
            try {
                Map<String, String> map = new LinkedHashMap<>();

                if (result.isClosed() || result.getObject(1) == null) {
                    return map;
                }

                for (int row = 1; row <= result.getMetaData().getColumnCount(); row++) {
                    map.put(result.getMetaData().getColumnName(row), result.getString(row));
                }
                return map;
            } catch (SQLException e) {
                throw new RuntimeException("Cannot get results data of a SQLiteDataResult", e);
            }
        }
        return new HashMap<>();
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
