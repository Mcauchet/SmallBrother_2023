package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class ReglagesActivity  extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_reglages.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reglages);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();
    }


    // --> Au clic que le bouton "Retour".
    public void retour(View view)
    {
        vibreur.vibration(context, 100);

        // Transition vers la AidantActivity.
        Intent intent = new Intent(context, AidantActivity.class);
        startActivityForResult(intent, 1);
    }


    // --> Au clic que le bouton "Aide".
    public void aide(View view)
    {
        vibreur.vibration(context, 100);

        // Ouverture de l'aide.
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(userdata.getUrl() + userdata.getHelp()));
        startActivity(browserIntent);
    }


    // --> Au clic que le bouton "btn_reinit_1".
    public void reinitAidant(View view)
    {
        vibreur.vibration(context, 330);

        // Demande de confirmation.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(true);
        builder.setTitle(getString(R.string.message02_titre));
        builder.setMessage(getString(R.string.message02_texte));
        builder.setPositiveButton(getString(R.string.oui), new DialogInterface.OnClickListener()
        {
            // Si choix = "OUI" :
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                vibreur.vibration(context, 100);

                userdata.byeData(); // Suppression des données de l'utilisateur.

                message(getString(R.string.message03A)); // toast de confirmation.

                // Redémarrage de l'appli.
                Intent mIntent = new Intent(context,Launch1Activity.class);
                startActivity(mIntent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
        {
            // Si choix = "ANNULER" :
            @Override
            public void onClick(DialogInterface dialog, int which) { /* rien */ }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // --> Au clic que le bouton "btn_reinit_2".
    public void reinitAide(View view)
    {
        vibreur.vibration(context, 330);

        // Demande de confirmation.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(true);
        builder.setTitle(getString(R.string.message02_titre));
        builder.setMessage(getString(R.string.message02_texte));
        builder.setPositiveButton(getString(R.string.oui), new DialogInterface.OnClickListener()
        {
            // Si choix = "OUI" :
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                vibreur.vibration(context, 200);

                userdata.loadData(); // Raptatriement des données de l'utilisateur.

                // Concoction et envoi du SMS.
                String sms = getString(R.string.smsys01);
                sms = sms.replace("§%", userdata.getNom());
                SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

                message(getString(R.string.message03B)); // toast de confirmation.
                userdata.refreshLog(3);            // message de Log adéquat.
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
        {
            // Si choix = "ANNULER" :
            @Override
            public void onClick(DialogInterface dialog, int which) { /* rien */ }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // --> Au clic que le bouton "btn_reinit_3".
    public void refairePhoto(View view)
    {
        vibreur.vibration(context, 200);

        // Changement d'activité.
        Intent mIntent = new Intent(context,PicActivity.class);
        startActivity(mIntent);
    }


    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    public void message(String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        vibreur.vibration(context, 330);
    }
}