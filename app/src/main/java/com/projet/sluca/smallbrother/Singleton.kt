package com.projet.sluca.smallbrother

import android.app.Application
import com.projet.sluca.smallbrother.models.UserData

class Singleton: Application() {

    lateinit var userData: UserData

    override fun onCreate() {
        super.onCreate()

        userData = UserData()
        userData.loadData(this)
    }
}