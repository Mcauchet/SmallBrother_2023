package com.projet.sluca.smallbrother;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PhoneStatReceiver extends BroadcastReceiver
{
    static String callNumber = "";
    static UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.

    @Override
    public void onReceive(Context context, Intent intent)
    {
        userdata.loadData();

        if(userdata.getTelephone() != null)
        {
            if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
            {
            /*
            incomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i(TAG, "call OUT:"+phoneNumber);
            */
            }
            else
            {
                TelephonyManager tm =(TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);

                switch (tm.getCallState())
                {
                    case TelephonyManager.CALL_STATE_RINGING:

                        // Set du numéro de l'appelant.
                        this.callNumber = intent.getStringExtra("incoming_number");

                        if(SmsReceiver.getBit() == 1) // Si le Mode Privé est activé.
                        {
                            // Avertir :
                            SmsReceiver.setBit(3); // cas d'un appel
                        }
                        else
                        {
                            // Déclaration d'un passage dans la WorkActivity pour éviter que, au retour dans
                            // AideActivity, ne soit généré un doublon du Handler local.
                            userdata.setEsquive(true);

                            // lancement de la "WorkActivity".
                            Intent intnt = new Intent(context, WorkActivity.class);
                            intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intnt);
                        }
                }
            }
        }
    }

    static String catchcallNumber() { return callNumber; }  // Getter du numéro de l'appelant.
    static void resetCallNumber()   { callNumber = ""; }    // Réinitialiser.
}
