package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class ExpenseController {

    @FXML private TextField categoryField, amountField, descriptionField, salaryField;
    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel, finalBalanceLabel;
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> colCategory, colDescription;
    @FXML private TableColumn<Expense, Double> colAmount, colBalance;
    @FXML private TableColumn<Expense, LocalDate> colDate;

    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private Connection conn;
    private double currentSalary = 0.0;

    // --------------------- INITIALIZATION ---------------------
    @FXML
    public void initialize() {
        conn = DatabaseConnection.connect();
        setupTableColumns();
        loadExpensesFromDB();
        setupListeners();
        expenseTable.setItems(expenses);
    }

    // --------------------- TABLE CONFIG ---------------------
    private void setupTableColumns() {
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colBalance.setCellValueFactory(c -> c.getValue().balanceProperty().asObject());
    }

    // --------------------- EVENT LISTENERS ---------------------
    private void setupListeners() {
        // When salary changes, recalculate balances
        salaryField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                currentSalary = Double.parseDouble(newVal);
            } catch (NumberFormatException e) {
                currentSalary = 0.0;
            }
            recomputeAllBalances();
            updateBalancesInDB();
        });

        // Fill fields when a row is selected
        expenseTable.setOnMouseClicked(e -> {
            Expense s = expenseTable.getSelectionModel().getSelectedItem();
            if (s != null) {
                categoryField.setText(s.getCategory());
                amountField.setText(String.valueOf(s.getAmount()));
                descriptionField.setText(s.getDescription());
                datePicker.setValue(s.getDate());
            }
        });
    }

    // --------------------- LOAD DATA FROM DATABASE ---------------------
    private void loadExpensesFromDB() {
        expenses.clear();
        String sql = "SELECT category, amount, description, date, balance, totalsalary FROM expenses";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Expense ex = new Expense(
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("date"))
                );
                ex.setBalance(rs.getDouble("balance"));
                expenses.add(ex);
            }

            recomputeAllBalances();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --------------------- ADD EXPENSE ---------------------
    @FXML
    private void addExpense() {
        String category = categoryField.getText();
        String amountText = amountField.getText();
        String description = descriptionField.getText();
        LocalDate date = datePicker.getValue();

        if (category.isEmpty() || amountText.isEmpty() || date == null) {
            statusLabel.setText("⚠️ Please fill in all required fields!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ Invalid amount!");
            return;
        }

        double balance = calculateRunningBalanceAfterAdding(amount);

        String sql = "INSERT INTO expenses (category, amount, description, date, balance, totalsalary) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setDouble(2, amount);
            ps.setString(3, description);
            ps.setString(4, date.toString());
            ps.setDouble(5, balance);
            ps.setDouble(6, currentSalary); // store total salary value
            ps.executeUpdate();

            Expense ex = new Expense(category, amount, description, date);
            ex.setBalance(balance);
            expenses.add(ex);

            recomputeAllBalances();
            updateBalancesInDB();

            statusLabel.setText("✅ Expense added!");
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Failed to add expense!");
        }
    }

    // --------------------- UPDATE EXPENSE ---------------------
    @FXML
    private void updateExpense() {
        Expense sel = expenseTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            statusLabel.setText("⚠️ Select a row to update!");
            return;
        }

        try {
            double newAmount = Double.parseDouble(amountField.getText());
            String sql = "UPDATE expenses SET category=?, amount=?, description=?, date=?, balance=?, totalsalary=? " +
                    "WHERE category=? AND amount=? AND date=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, categoryField.getText());
            ps.setDouble(2, newAmount);
            ps.setString(3, descriptionField.getText());
            ps.setString(4, datePicker.getValue().toString());
            ps.setDouble(5, calculateRunningBalanceAfterAdding(newAmount));
            ps.setDouble(6, currentSalary);
            ps.setString(7, sel.getCategory());
            ps.setDouble(8, sel.getAmount());
            ps.setString(9, sel.getDate().toString());
            ps.executeUpdate();

            sel.setCategory(categoryField.getText());
            sel.setAmount(newAmount);
            sel.setDescription(descriptionField.getText());
            sel.setDate(datePicker.getValue());
            recomputeAllBalances();
            updateBalancesInDB();

            statusLabel.setText("✅ Expense updated!");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Update failed!");
        }
    }

    // --------------------- DELETE EXPENSE ---------------------
    @FXML
    private void deleteExpense() {
        Expense sel = expenseTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            statusLabel.setText("⚠️ Select an expense to delete!");
            return;
        }

        String sql = "DELETE FROM expenses WHERE category=? AND amount=? AND date=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sel.getCategory());
            ps.setDouble(2, sel.getAmount());
            ps.setString(3, sel.getDate().toString());
            ps.executeUpdate();

            expenses.remove(sel);
            recomputeAllBalances();
            updateBalancesInDB();

            statusLabel.setText("🗑 Expense deleted!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --------------------- BALANCE CALCULATIONS ---------------------
    private void recomputeAllBalances() {
        double runningBalance = currentSalary;
        for (Expense ex : expenses) {
            runningBalance -= ex.getAmount();
            ex.setBalance(runningBalance);
        }
        expenseTable.refresh();

        if (finalBalanceLabel != null)
            finalBalanceLabel.setText(String.format("₹ %.2f", runningBalance));
    }

    private double calculateRunningBalanceAfterAdding(double newAmount) {
        double runningBalance = currentSalary;
        for (Expense ex : expenses) {
            runningBalance -= ex.getAmount();
        }
        runningBalance -= newAmount;
        return runningBalance;
    }

    private void updateBalancesInDB() {
        String sql = "UPDATE expenses SET balance=?, totalsalary=? WHERE category=? AND amount=? AND date=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Expense ex : expenses) {
                ps.setDouble(1, ex.getBalance());
                ps.setDouble(2, currentSalary);
                ps.setString(3, ex.getCategory());
                ps.setDouble(4, ex.getAmount());
                ps.setString(5, ex.getDate().toString());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --------------------- CLEAR INPUT FIELDS ---------------------
    private void clearFields() {
        categoryField.clear();
        amountField.clear();
        descriptionField.clear();
        datePicker.setValue(null);
    }
}
