package com.projet.sluca.smallbrother

import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData

/**
 * PhoneStatReceiver manages information when a phone call is received on the user's phone
 *
 * @author Sébastien Luca & Maxime Caucheteur (Updated on 08-01-2023)
 */
class PhoneStatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //userData = UserDataManager.getUserData(context.applicationContext as Application)
        userData.loadData(context)
        if (intent.action != Intent.ACTION_NEW_OUTGOING_CALL) processCall(context, intent)
    }

    /**
     * Process the phone call
     * @param [context] the context of the activity
     * @param [intent] the intent on receive of a call
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun processCall(context: Context, intent: Intent) {
        val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        if (tm.callState == TelephonyManager.CALL_STATE_RINGING) {
            if(intent.hasExtra("incoming_number")) callNumber = intent
                .getStringExtra("incoming_number").toString()
            if (userData.bit == 1) userData.bit = 3 // Private mode ON
            else {
                userData.esquive = true
                val intnt = Intent(context, WorkActivity::class.java)
                intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intnt)
            }
        }
    }

    companion object {
        var callNumber = ""
        lateinit var userData: UserData 

        fun catchCallNumber(): String = callNumber
        fun resetCallNumber() {
            callNumber = ""
        }
    }
}
