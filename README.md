<!-- ========================================================= -->
<!-- 🎨 EXPENSE TRACKER - GITHUB README FILE BY INDHIRAN S     -->
<!-- ========================================================= -->

<h1 align="center">💰 Expense Tracker</h1>
<h3 align="center">A Modern JavaFX + SQLite Desktop Application for Smart Expense Management</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Built%20With-JavaFX-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Database-SQLite-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Language-Java-yellow?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
</p>

---

## 🧾 Overview

**Expense Tracker 💰** is a powerful and user-friendly **JavaFX desktop application** that helps you record, manage, and analyze your daily expenses — all stored permanently using **SQLite**.  
With its **dark-themed UI**, **real-time total expense updates**, and **intuitive controls**, it offers the perfect blend of functionality and elegance.  

This project showcases **end-to-end JavaFX development** — from sleek UI design to efficient backend integration — making it a great example for students, professionals, and developers learning **JavaFX + JDBC**.

---

## 🌟 Features

✅ **Add / Edit / Delete Expenses**  
Easily manage your expense records with a clean and interactive interface.

✅ **Permanent Storage with SQLite**  
All data is saved locally inside `expenses.db` and automatically loaded every time you open the app.

✅ **Search & Filter in Real-Time**  
Type in the search bar to instantly filter expenses by category or description.

✅ **Dynamic Total Expense Display**  
Your total spending automatically updates whenever you add, update, or delete an expense.

✅ **Dark-Themed Modern UI**  
Built using JavaFX CSS with gradient backgrounds, glowing fields, and smooth animations.

✅ **Reload Data Instantly**  
Syncs the table with the database in one click — no need to restart the app.

---

## 🧠 Tech Stack

| Layer | Technology |
|--------|-------------|
| **Frontend (UI)** | JavaFX, FXML, CSS |
| **Backend (Logic)** | Java |
| **Database** | SQLite |
| **Driver** | sqlite-jdbc-3.51.0.0.jar |
| **SDK** | JavaFX SDK 25.0.1 |
| **JDK Version** | 17 or later |
| **IDE (optional)** | Eclipse / IntelliJ / VS Code |

---

## 🧠 Tech Stack

| Layer | Technology |
|--------|-------------|
| **Frontend (UI)** | JavaFX, FXML, CSS |
| **Backend (Logic)** | Java |
| **Database** | SQLite |
| **Driver** | sqlite-jdbc-3.51.0.0.jar |
| **SDK** | JavaFX SDK 25.0.1 |
| **JDK Version** | 17 or later |
| **IDE (optional)** | Eclipse / IntelliJ / VS Code |

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


