package codes.laivy.data.sql.mysql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.query.DataStatement;
import codes.laivy.data.sql.sqlite.SQLiteResult;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLStatement extends DataStatement {

    private PreparedStatement statement;

    public MySQLStatement(@NotNull MySQLDatabase database, @NotNull String query) {
        super(database, query);

        try {
            statement = ((MySQLDatabaseType) database.getDatabaseType()).getConnection().prepareStatement(getQuery());
        } catch (Throwable e) {
            database.getDatabaseType().throwError(e);
        }
    }

    @Override
    public @NotNull MySQLResult execute() {
        if (statement == null) {
            if (DataAPI.DEBUG) System.out.println("Couldn't execute the 'execute' method of SQLiteStatement bacause the statement didn't have created successfully");
            return new MySQLResult(null);
        }

        try {
            try {
                return new MySQLResult(statement.executeQuery());
            } catch (SQLException ex) {
                if (ex.getMessage().equals("Query does not return results")) {
                    statement.executeUpdate();
                    return new MySQLResult(null);
                } else {
                    throw new RuntimeException("Couldn't execute/update MySQL statement", ex);
                }
            }
        } catch (Throwable e) {
            getDatabase().getDatabaseType().throwError(e);
        }
        return new MySQLResult(null);
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
