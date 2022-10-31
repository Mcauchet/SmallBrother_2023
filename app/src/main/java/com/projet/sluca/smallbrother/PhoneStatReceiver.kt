package com.projet.sluca.smallbrother

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager


class PhoneStatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        userdata.loadData()
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
                callNumber = intent.getStringExtra("incoming_number").toString()
                if (userdata.bit == 1) // Si le Mode Privé est activé.
                {
                    // Avertir :
                    userdata.bit = 3 // cas d'un appel
                } else {
                    // Déclaration d'un passage dans la WorkActivity pour éviter que, au retour dans
                    // AideActivity, ne soit généré un doublon du Handler local.
                    userdata.esquive = true

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
        lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.

        fun catchcallNumber(): String = callNumber // Getter du numéro de l'appelant.

        fun resetCallNumber() {
            callNumber = ""
        } // Réinitialiser.
    }
}
