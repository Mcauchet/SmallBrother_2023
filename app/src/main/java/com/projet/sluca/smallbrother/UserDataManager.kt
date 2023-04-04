package com.projet.sluca.smallbrother

import android.app.Application
import com.projet.sluca.smallbrother.models.UserData

/**
 * UserDataManager acts as a Singleton for the userData class invoked in activities
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-04-2023)
 */
object UserDataManager {

    private var userData: UserData? = null

    /**
     * Returns the userData object
     * @param application
     * @return userData
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-04-2023)
     */
    fun getUserData(application: Application): UserData {
        if(userData == null) {
            userData = application as UserData
            userData!!.loadData(application.applicationContext)
        }
        return userData!!
    }
}