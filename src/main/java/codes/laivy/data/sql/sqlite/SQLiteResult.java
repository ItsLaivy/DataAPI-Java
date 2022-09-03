package codes.laivy.data.sql.sqlite;

import codes.laivy.data.query.DataResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SQLiteResult extends DataResult {

    private final ResultSet result;

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
                Map<String, String> map = new TreeMap<>();

                if (result.getObject(1) == null) {
                    return map;
                }

                for (int row = 1; row <= result.getMetaData().getColumnCount(); row++) {
                    map.put(result.getMetaData().getColumnName(row), result.getString(row));
                }
                return map;
            } catch (SQLException e) {
                throw new RuntimeException("Cannot get columns data of a SQLiteDataResult", e);
            }
        }
        return new HashMap<>();
    }
}
