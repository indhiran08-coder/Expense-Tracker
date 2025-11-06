package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:expenses.db";
    private static Connection conn = null;

    // ---------------- CONNECT ----------------
    public static Connection connect() {
        if (conn != null) return conn; // reuse same connection
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Connected to SQLite database successfully!");
            createAndUpgradeTable(conn);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
        return conn;
    }

    // ---------------- CREATE + UPGRADE TABLE ----------------
    private static void createAndUpgradeTable(Connection conn) {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                description TEXT,
                date TEXT NOT NULL,
                balance REAL,
                totalsalary REAL
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Table 'expenses' verified or created.");

            // Ensure balance column exists
            try {
                stmt.execute("ALTER TABLE expenses ADD COLUMN balance REAL DEFAULT 0;");
                System.out.println("🟢 'balance' column added successfully!");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    System.out.println("ℹ️ 'balance' column already exists.");
                } else {
                    System.err.println("⚠️ Error adding 'balance' column: " + e.getMessage());
                }
            }

            // Ensure totalsalary column exists
            try {
                stmt.execute("ALTER TABLE expenses ADD COLUMN totalsalary REAL DEFAULT 0;");
                System.out.println("🟢 'totalsalary' column added successfully!");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column name")) {
                    System.out.println("ℹ️ 'totalsalary' column already exists.");
                } else {
                    System.err.println("⚠️ Error adding 'totalsalary' column: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error verifying/creating table: " + e.getMessage());
        }
    }

    // ---------------- GET CONNECTION ----------------
    public static Connection getConnection() {
        if (conn == null) return connect();
        return conn;
    }

    // ---------------- CLOSE CONNECTION ----------------
    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔒 Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
