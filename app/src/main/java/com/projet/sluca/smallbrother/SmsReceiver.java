package com.projet.sluca.smallbrother;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

// Classe vouée à l'écoute de l'arrivée des SMS.

public class SmsReceiver extends BroadcastReceiver
{
    static String numero;  // Retiendra le numéro de l'envoyeur.
    static String clef;    // Retiendra le mot-clef du sms.

    static UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.

    static String tempsrestant; // Retiendra le temps restant de Mode Privé pour l'Aidant.


    @Override
    public void onReceive(Context context, Intent intent) // Lors d'une réception de SMS :
    {
        String message = "";
        Bundle bundle = intent.getExtras();
        SmsMessage[] sms = null;

        userdata.loadData();

        if (bundle != null && userdata.getTelephone() != null)
        {
            // Récupération du SMS reçu.
            Object[] pdus = (Object[]) bundle.get("pdus");
            sms = new SmsMessage[pdus.length];

            this.clef = ""; // Réinitialisation de la valeur de la clef.

            for (int i = 0; i < sms.length; i++)
            {
                // Tri et mémorisation du contenu du SMS.
                sms[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                this.numero = sms[i].getOriginatingAddress(); // Set du numéro.
                message = sms[i].getMessageBody();
            }

            if(message.startsWith("SmallBrother :")) // Si SMS destiné à l'appli.
            {
                // Isolement du code d'identification, en fin de SMS (7 caras).
                clef = message.substring(message.length() - 7);

                String[] motsclef =         // Liste de mots-clefs de déclenchement.
                        {
                                "[#SB01]",  // -> réinit aidé
                                "[#SB02]",  // -> va bien reçu par aidé
                                "[#SB03]",  // -> oui va bien reçu par aidant
                                "[#SB04]",  // -> urgence reçue par aidé
                                "[#SB05]",  // -> aidé pas connecté
                                "[#SB06]",  // -> mail d'urgence reçu
                                "[#SB07]"   // -> mode privé activé
                        };

                if (Arrays.asList( motsclef ).contains( clef ))
                {
                    if(getBit() == 1) // Si le Mode Privé est activé.
                    {
                        // Avertir :
                             if(clef.equals("[#SB02]")) setBit(2); // cas d'un SMS
                        else if(clef.equals("[#SB04]")) setBit(4); // cas d'un email
                    }
                    else
                    {
                        if(clef.equals("[#SB07]")) // Récupération du temps restant si Mode Privé.
                        {
                            String extrait = message.substring(message.indexOf("(") + 1, message.indexOf(")"));
                            tempsrestant = extrait;
                        }

                        // lancement de la "WorkActivity".
                        Intent intnt = new Intent(context, WorkActivity.class);
                        intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intnt);
                    }
                }
            }
        }
    }

    static String catchNumero() { return numero; } // Getter du numéro.
    static String catchClef()   { return clef; }   // Getter du mot-clef.

    static String catchTempsRestant() { return tempsrestant; } // Getter du temps restant.


    // ---- Méchanismes de communication AideActivity / SmsReceiver.

    static int getBit()
    {
        File data = new File( userdata.getPath() + "bit.txt" );

        if (data.exists())
        {
            try // Récupération du contenu du fichier :
            {
                // Placement des données dans un array, séparation par le retour-charriot.
                BufferedReader br = new BufferedReader(new FileReader(data));
                String dataLine = org.apache.commons.io.IOUtils.toString(br);
                String[] dataTab = dataLine.split("\r");

                // Rapatriement des données :
                int val = Integer.valueOf(dataTab[0]);

                return val;
            }
            catch (FileNotFoundException e) { e.printStackTrace(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        return 0;
    }

    static void setBit(int bit)
    {
        try
        {
            File bitFile = new File( userdata.getPath() + "bit.txt" );
            if (!bitFile.exists()) bitFile.createNewFile();
            else
            {
                // Suppression du fichier de données s'il existe déjà (pour éviter concaténation).
                File ciao = new File( userdata.getPath() + "bit.txt" );
                ciao.delete();
            }

            // Ecriture.
            BufferedWriter writer = new BufferedWriter(new FileWriter(bitFile, true));
            writer.write(String.valueOf( bit ));
            writer.close();
        }
        catch (IOException e){ }
    }
}