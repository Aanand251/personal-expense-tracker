# 💰 Personal Expense Tracker

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Room](https://img.shields.io/badge/Room-Database-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A smart, feature-rich Android app to track expenses, manage loans, predict spending, and stay on top of your budget!**

### ⬇️ [Download Latest APK](../../releases/latest)

</div>

---

## ✨ Features

### 💵 Smart Expense Management
- Add and categorize daily expenses
- Set **monthly income** manually
- Set **fixed expenses** (rent, EMI, utilities)
- **Smart blocking** — Cannot add expense if no funds available
- **PDF report** download of all expenses
- Delete expenses with confirmation

### 🔔 Intelligent Notifications
- Notified when **expenses exceed income**
- Auto **deduct from savings** when overspending
- Auto **save remaining income** to savings

### 💰 Savings Management
- Automatic savings calculation after every expense
- View total savings on home screen

### 📊 Analytics and Predictions
- Beautiful **Pie Chart** for expense breakdown
- **Advanced Analytics** — Spending trends and insights
- **AI-based Expense Predictions** — Predicts future spending

### 🏦 Loan Management
- Add and track **loans** (borrowed/lent)
- View all active loans
- Mark loans as paid

### 🧾 Receipt Scanner
- **Scan receipts** using camera
- Auto-extract expense data from receipts

### 🔐 User Authentication
- **Firebase Authentication** — Secure login/signup
- **User-specific data** — Each user has their own data

### 📄 PDF Export
- Export all expenses as a **PDF report**
- Saved to Downloads folder
- Works on all Android versions

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary programming language |
| **Android SDK 35** | Target platform (Min SDK 24) |
| **Firebase Auth** | User authentication |
| **Firebase Firestore** | Cloud database |
| **Room Database** | Local offline database |
| **ViewModel + LiveData** | MVVM Architecture |
| **Coroutines + Flow** | Async operations |
| **MPAndroidChart** | Charts and graphs |
| **iText PDF** | PDF report generation |
| **Material Design 3** | Modern UI components |

---

## 🚀 Installation

### Option 1: Download APK (Easiest!)
1. Go to **[Releases](../../releases/latest)**
2. Download `app-debug.apk`
3. On your phone: **Settings → Security → Allow Unknown Sources**
4. Install the APK and enjoy!

### Option 2: Build from Source
```bash
git clone https://github.com/Aanand251/personal-expense-tracker.git
```
- Open in Android Studio
- Add your `google-services.json` to `app/` folder
- Build and Run

---

## ⚙️ Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project
3. Add Android app with package: `com.example.personalexpensetracker`
4. Download `google-services.json` and place in `app/` folder
5. Enable **Authentication** (Email/Password) and **Firestore Database**

---

## 📋 Requirements

- Android 7.0 (API 24) or higher
- Internet connection (for Firebase)
- Camera permission (for Receipt Scanner)
- Notification permission (Android 13+)

---

## 👨‍💻 Developer

**Aanand251** — [@Aanand251](https://github.com/Aanand251)

---

## ⭐ Support

If you like this project, please give it a **Star** on GitHub!

[![Star on GitHub](https://img.shields.io/github/stars/Aanand251/personal-expense-tracker?style=social)](https://github.com/Aanand251/personal-expense-tracker)

---

## 📄 License

MIT License — Copyright (c) 2026 Aanand251
