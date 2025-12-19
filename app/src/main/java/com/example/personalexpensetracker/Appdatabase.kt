package com.example.personalexpensetracker
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
@Database(entities = [Expense::class, Savings::class, Loan::class, LoanPayment::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun savingsDao(): SavingsDao
    abstract fun loanDao(): LoanDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense ADD COLUMN name TEXT")
            }
        }
        val MIGRATION_3_4 = object : Migration(3,4){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense ADD COLUMN category TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        personName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        interestRate REAL NOT NULL DEFAULT 0.0,
                        status TEXT NOT NULL,
                        paidAmount REAL NOT NULL DEFAULT 0.0,
                        description TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS loan_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        loanId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}