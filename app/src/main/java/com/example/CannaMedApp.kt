package com.example

import android.app.Application
import com.example.data.repository.CannaMedRepository

class CannaMedApp : Application() {
    lateinit var repository: CannaMedRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = CannaMedRepository(this)
    }
}
