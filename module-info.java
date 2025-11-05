module ExpenseTracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql; // ✅ Add this line for database support

    opens application to javafx.graphics, javafx.fxml;
    exports application;
}
