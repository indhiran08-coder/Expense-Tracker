package application;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Expense {
    private final StringProperty category;
    private final DoubleProperty amount;
    private final StringProperty description;
    private final ObjectProperty<LocalDate> date;

    public Expense(String category, double amount, String description, LocalDate date) {
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleObjectProperty<>(date);
    }

    public StringProperty categoryProperty() { return category; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public String getCategory() { return category.get(); }
    public double getAmount() { return amount.get(); }
    public String getDescription() { return description.get(); }
    public LocalDate getDate() { return date.get(); }

    public void setCategory(String value) { category.set(value); }
    public void setAmount(double value) { amount.set(value); }
    public void setDescription(String value) { description.set(value); }
    public void setDate(LocalDate value) { date.set(value); }
}
