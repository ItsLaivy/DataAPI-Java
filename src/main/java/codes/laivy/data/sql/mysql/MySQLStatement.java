package codes.laivy.data.sql.mysql;

import codes.laivy.data.DataAPI;
import codes.laivy.data.query.DataStatement;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLStatement extends DataStatement {

    private final @Nullable PreparedStatement statement;
    private final @NotNull MySQLDatabaseType type;

    public @Nullable PreparedStatement getStatement() {
        return statement;
    }

    public MySQLStatement(@NotNull MySQLDatabaseType type, @NotNull String query) {
        super(query);

        this.type = type;

        try {
            statement = getType().getConnection().prepareStatement(getQuery());
        } catch (Throwable e) {
            getType().throwError(e);
            throw new RuntimeException(e);
        }
    }

    public @NotNull MySQLDatabaseType getType() {
        return type;
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
                if (
                        ex.getMessage().equals("Query does not return results") ||
                        ex.getMessage().equals("Statement.executeQuery() cannot issue statements that do not produce result sets.")
                ) {
                    statement.executeUpdate();
                    return new MySQLResult(null);
                } else {
                    throw new RuntimeException("Couldn't execute/update MySQL statement", ex);
                }
            }
        } catch (CommunicationsException e) {
            if (e.getMessage().contains("The last packet successfully received from the server was")) {
                getType().close();
                getType().open();

                new MySQLStatement(type, getQuery()).execute();
            } else {
                getType().throwError(e);
            }
        } catch (Throwable e) {
            getType().throwError(e);
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
            getType().throwError(e);
        }
    }
}
