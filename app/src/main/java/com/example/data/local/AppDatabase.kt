package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.data.model.StrainInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DosageLog::class,
        Cartridge::class,
        Prescription::class,
        StrainInfo::class,
        LegalExtract::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dosageLogDao(): DosageLogDao
    abstract fun cartridgeDao(): CartridgeDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun strainDao(): StrainDao
    abstract fun legalExtractDao(): LegalExtractDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cannamed_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.strainDao().insertAll(InitialData.DEFAULT_STRAINS)
                                database.cartridgeDao().insert(InitialData.createInitialCartridge())
                                database.prescriptionDao().insert(InitialData.createInitialPrescription())
                                database.legalExtractDao().insertAll(InitialData.DEFAULT_LEGAL_EXTRACTS)
                                for (log in InitialData.createInitialLogs()) {
                                    database.dosageLogDao().insert(log)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
