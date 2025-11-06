# 💰 Expense Tracker – JavaFX + SQLite

### 🔹 Smart Desktop Application to Manage Your Daily Expenses with Balance Tracking and Salary Management

Expense Tracker is a **JavaFX-based personal finance management tool** that allows users to record, update, and delete daily expenses while automatically calculating running balances based on their total monthly salary.  
Built with **JavaFX, SQLite**, and **JDBC**, this lightweight yet powerful application provides both visual insights and persistent storage of your spending data.

---

## 🧩 Features

✨ **Add, Edit, and Delete Expenses**  
Easily add expense entries with a category, amount, description, and date.

🏦 **Salary Integration**  
Input your monthly salary once — the app automatically calculates your remaining balance as you add or edit expenses.

📊 **Running Balance per Transaction**  
Each expense row dynamically updates the **running balance** (salary - total expenses up to that point).

💾 **SQLite Database Storage**  
All expenses are permanently saved in a local SQLite database (`expenses.db`), ensuring your data persists across sessions.

📈 **Auto-Updating Database Schema**  
No need to recreate your database — the app automatically adds new columns (`balance`, `totalsalary`) if missing.

💬 **Real-Time Feedback**  
Displays status messages for every operation (Add, Update, Delete) directly in the UI.

🧮 **Dynamic Remaining Balance Display**  
Shows the final remaining balance at the bottom of the app for clear financial tracking.

---

## 🏗️ Tech Stack

| Layer | Technology Used |
|--------|-----------------|
| **Frontend** | JavaFX (FXML + CSS) |
| **Backend** | Java (OOP + JDBC) |
| **Database** | SQLite |
| **IDE Support** | Eclipse / IntelliJ / VS Code |
| **UI Loader** | FXML with `ExpenseController.java` |
| **Build Tools** | JavaFX SDK 25, sqlite-jdbc 3.51.0.0 |

---

## 📁 Folder Structure


ExpenseTracker/
│
├── src/
│ └── application/
│ ├── Main.java
│ ├── Expense.java
│ ├── ExpenseController.java
│ ├── DatabaseConnection.java
│ ├── ExpenseTracker.fxml
│ └── style.css
│
├── lib/
│ ├── javafx-sdk-25.0.1 jars
│ └── sqlite-jdbc-3.51.0.0.jar
│
├── module-info.java
└── expenses.db (auto-created SQLite database)

| Column      | Type                  | Description                           |
| ----------- | --------------------- | ------------------------------------- |
| id          | INTEGER (Primary Key) | Auto-generated ID                     |
| category    | TEXT                  | Expense category (e.g., Food, Travel) |
| amount      | REAL                  | Amount spent                          |
| description | TEXT                  | Description of expense                |
| date        | TEXT                  | Date of expense                       |

🪶 License

This project is released under the MIT License.
You can use, modify, and distribute it freely for educational or personal purposes.

💡 Summary

Expense Tracker 💰 is a complete desktop finance manager built with JavaFX + SQLite, combining smooth UI, live data updates, and local storage — all in one app.
It’s lightweight, fast, and reliable — a perfect balance between performance and simplicity.

“Track your spending. Manage your money. Master your finances.”
— Built with ❤️ by Indhiran S


