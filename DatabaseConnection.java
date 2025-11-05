package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:expenses.db";

    // Establish and return connection
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTable(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Create table if not exists
    private static void createTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS expenses (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "category TEXT NOT NULL," +
                     "amount REAL NOT NULL," +
                     "description TEXT," +
                     "date TEXT NOT NULL" +
                     ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
