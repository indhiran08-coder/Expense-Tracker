package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseController {

    @FXML
    private TextField categoryField, amountField, descriptionField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView<Expense> expenseTable;

    @FXML
    private TableColumn<Expense, String> colCategory;

    @FXML
    private TableColumn<Expense, Double> colAmount;

    @FXML
    private TableColumn<Expense, String> colDescription;

    @FXML
    private TableColumn<Expense, LocalDate> colDate;

    @FXML
    private Label statusLabel;

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private Connection conn;

    @FXML
    public void initialize() {
        conn = DatabaseConnection.connect();

        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        expenseTable.setItems(expenses);

        loadExpensesFromDB();
    }

    // ✅ Load all expenses from the database
    private void loadExpensesFromDB() {
        expenses.clear();
        String sql = "SELECT * FROM expenses";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                expenses.add(new Expense(category, amount, description, date));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Add Expense
    @FXML
    private void addExpense() {
        try {
            String category = categoryField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            LocalDate date = datePicker.getValue();

            if (category.isEmpty() || date == null) {
                statusLabel.setText("⚠️ Fill all fields!");
                statusLabel.setStyle("-fx-text-fill: #ff5555;");
                return;
            }

            String sql = "INSERT INTO expenses(category, amount, description, date) VALUES(?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, description);
            pstmt.setString(4, date.toString());
            pstmt.executeUpdate();

            expenses.add(new Expense(category, amount, description, date));
            clearFields();

            statusLabel.setText("✅ Expense added successfully!");
            statusLabel.setStyle("-fx-text-fill: #00ff99;");

        } catch (Exception e) {
            statusLabel.setText("❌ Invalid input!");
            statusLabel.setStyle("-fx-text-fill: #ff5555;");
            e.printStackTrace();
        }
    }

    // ✅ Update Expense
    @FXML
    private void updateExpense() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Select an expense to update!");
            statusLabel.setStyle("-fx-text-fill: #ffcc00;");
            return;
        }

        try {
            String category = categoryField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            LocalDate date = datePicker.getValue();

            String sql = "UPDATE expenses SET category=?, amount=?, description=?, date=? WHERE category=? AND amount=? AND date=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, description);
            pstmt.setString(4, date.toString());
            pstmt.setString(5, selected.getCategory());
            pstmt.setDouble(6, selected.getAmount());
            pstmt.setString(7, selected.getDate().toString());
            pstmt.executeUpdate();

            loadExpensesFromDB();
            clearFields();
            statusLabel.setText("✅ Expense updated!");
            statusLabel.setStyle("-fx-text-fill: #00ff99;");

        } catch (Exception e) {
            statusLabel.setText("❌ Update failed!");
            statusLabel.setStyle("-fx-text-fill: #ff5555;");
            e.printStackTrace();
        }
    }

    // ✅ Delete Expense
    @FXML
    private void deleteExpense() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Select an expense to delete!");
            statusLabel.setStyle("-fx-text-fill: #ffcc00;");
            return;
        }

        try {
            String sql = "DELETE FROM expenses WHERE category=? AND amount=? AND date=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getCategory());
            pstmt.setDouble(2, selected.getAmount());
            pstmt.setString(3, selected.getDate().toString());
            pstmt.executeUpdate();

            expenses.remove(selected);
            statusLabel.setText("🗑️ Expense deleted!");
            statusLabel.setStyle("-fx-text-fill: #ff6666;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        categoryField.clear();
        amountField.clear();
        descriptionField.clear();
        datePicker.setValue(null);
    }
}
