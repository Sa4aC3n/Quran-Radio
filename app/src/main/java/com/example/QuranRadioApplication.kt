package com.example

import android.app.Application
import com.example.data.RadioRepository
import com.example.data.UserPreferencesRepository
import com.example.player.PlayerManager

class QuranRadioApplication : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var radioRepository: RadioRepository
        private set

    lateinit var playerManager: PlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        userPreferencesRepository = UserPreferencesRepository(this)
        radioRepository = RadioRepository(this)
        playerManager = PlayerManager.getInstance(this, userPreferencesRepository, radioRepository)
    }

    companion object {
        lateinit var instance: QuranRadioApplication
            private set
    }
}
