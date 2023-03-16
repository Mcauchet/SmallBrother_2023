package com.projet.sluca.smallbrother

import android.app.Application
import com.projet.sluca.smallbrother.models.UserData

object UserDataManager {

    private var userData: UserData? = null

    fun getUserData(application: Application): UserData {
        if(userData == null) {
            userData = application as UserData
            userData!!.loadData(application.applicationContext)
        }
        return userData!!
    }
}