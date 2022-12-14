package com.projet.sluca.smallbrother

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData

/***
 * PhoneStatReceiver manages information when a phone call is made between aidant and aide
 *
 * @author Sébastien Luca & Maxime Caucheteur (Updated on 14-12-22)
 */
class PhoneStatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        userData.loadData()
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            /*
        incomingFlag = false;
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.i(TAG, "call OUT:"+phoneNumber);
        */
        } else {
            val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            //TODO see deprecated
            if (tm.callState == TelephonyManager.CALL_STATE_RINGING) {
                // Set du numéro de l'appelant.
                if(intent.hasExtra("incoming_number")) {
                    callNumber = intent.getStringExtra("incoming_number").toString()
                }
                if (userData.bit == 1) // Si le Mode Privé est activé.
                {
                    // Avertir :
                    userData.bit = 3 // cas d'un appel
                } else {
                    // Déclaration d'un passage dans la WorkActivity pour éviter que, au retour dans
                    // AideActivity, ne soit généré un doublon du Handler local.
                    userData.esquive = true

                    // lancement de la "WorkActivity".
                    val intnt = Intent(context, WorkActivity::class.java)
                    intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intnt)
                }
            }
        }
    }

    companion object {
        var callNumber = ""
        lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.

        fun catchCallNumber(): String = callNumber

        fun resetCallNumber() {
            callNumber = ""
        }
    }
}
