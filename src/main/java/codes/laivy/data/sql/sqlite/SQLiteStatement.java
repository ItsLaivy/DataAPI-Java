package codes.laivy.data.sql.sqlite;

import codes.laivy.data.DataAPI;
import codes.laivy.data.query.DataStatement;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteStatement extends DataStatement {

    private PreparedStatement statement;

    public SQLiteStatement(@NotNull SQLiteDatabase database, @NotNull String query) {
        super(database, query);

        try {
            statement = database.getConnection().prepareStatement(getQuery());
        } catch (Throwable e) {
            database.getDatabaseType().throwError(e);
        }
    }

    @Override
    public @NotNull SQLiteResult execute() {
        if (statement == null) {
            if (DataAPI.DEBUG) System.out.println("Couldn't execute the 'execute' method of SQLiteStatement bacause the statement didn't have created successfully");
            return new SQLiteResult(null);
        }

        try {
            try {
                return new SQLiteResult(statement.executeQuery());
            } catch (SQLException ex) {
                if (ex.getMessage().equals("Query does not return results")) {
                    statement.executeUpdate();
                    return new SQLiteResult(null);
                } else {
                    throw new RuntimeException("Couldn't execute/update SQLite Statement", ex);
                }
            }
        } catch (Throwable e) {
            getDatabase().getDatabaseType().throwError(e);
        }
        return new SQLiteResult(null);
    }

    @Override
    public void close() {
        if (statement == null) {
            if (DataAPI.DEBUG) System.out.println("Couldn't execute the 'close' method of SQLiteStatement bacause the statement didn't have created successfully");
            return;
        }

        try {
            statement.close();
        } catch (Throwable e) {
            getDatabase().getDatabaseType().throwError(e);
        }
    }

}
