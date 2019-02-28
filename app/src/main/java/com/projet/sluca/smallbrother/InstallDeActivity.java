package com.projet.sluca.smallbrother;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InstallDeActivity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_installde.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installde);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        // Retrait du bouton retour, au cas où désactivé par ReglagesActivity.
        if(!userdata.canIGoBack())
        {
            Button btn = (Button) findViewById(R.id.btn_previous);
            btn.setVisibility(View.INVISIBLE);
        }

        // Lancement des demandes de permissions.
        demandesPermissions();
    }


    // --> Au clic que le bouton "Précédent".
    public void precedent(View view)
    {
        vibreur.vibration(context, 100);

        finish();
    }


    // --> Au clic que le bouton "Terminer".
    public void continuer(View view)
    {
        vibreur.vibration(context, 100);


        // > Récupération du contenu des inputs :

        // Nom :
        EditText et_nom = (EditText) findViewById(R.id.input_nom);
        String nom = et_nom.getText().toString();

        // Téléphone :
        EditText et_telephone = (EditText) findViewById(R.id.input_telephone);
        String telephone = et_telephone.getText().toString();

        // Email de l'Aidant :
        EditText et_email = (EditText) findViewById(R.id.input_email);
        String email = et_email.getText().toString();


        // > Vérification de la validité des informations entrées :

        // Vérification 1 : le numéro de téléphone n'a pas une structure vraisemblable.
        if(telephone.length() > 10 || (!telephone.matches("") && !telephone.startsWith("04")))
        {
            message(getString(R.string.error01));
        }
        // Vérification 2 : l'adresse email n'est pas crédible.
        else if(!email.matches("") && !email.contains ("@"))
        {
            message(getString(R.string.error02));
        }
        // Vérification 3 : aucune entrée n'est vide.
        else if(nom.matches("") || telephone.matches("") || email.matches(""))
        {
            message(getString(R.string.error03));
        }

        // > Validation si tout a été positivement vérifié :

        else
        {
            // Récupération de la version de SB en cours.
            String version = "";
            try { version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; }
            catch (PackageManager.NameNotFoundException e) { e.printStackTrace(); }

            // Sauvegarde en globale des valeurs entrées.
            userdata.setVersion(version);
            userdata.setNom(nom);
            userdata.setTelephone(telephone);
            userdata.setEmail(email);

            // Transition vers l'activity suivante.
            Intent intent = new Intent(context, InstallDe2Activity.class);
            startActivityForResult(intent, 1);
        }
    }


    // --> DEMANDESPERMISSIONS() : Liste des permissions requises pour ce rôle.
    public void demandesPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
            {
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // -> enregistrer un fichier.
                Manifest.permission.READ_EXTERNAL_STORAGE,  // -> lire un fichier.
                Manifest.permission.SEND_SMS,               // -> envoyer des SMS
                Manifest.permission.CALL_PHONE,             // -> passer des appels
                Manifest.permission.READ_SMS,               // -> lire les SMS
                Manifest.permission.RECEIVE_SMS,            // -> recevoir des SMS
                Manifest.permission.RECEIVE_BOOT_COMPLETED, // -> lancement d'activité
                Manifest.permission.READ_PHONE_STATE,       // -> infos du téléphones
                Manifest.permission.PROCESS_OUTGOING_CALLS, // -> passer des appels
                Manifest.permission.RECORD_AUDIO,           // -> enregistrer de l'audio.
                Manifest.permission.CAMERA,                 // -> utiliser l'appareil photo.
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // -> enregistrer un fichier.
                Manifest.permission.SEND_SMS,               // -> envoyer des SMS
                Manifest.permission.ACCESS_FINE_LOCATION    // -> localiser.
            }
            , 1);
        }
    }


    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    public void message(String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        vibreur.vibration(context, 330);
    }


    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    @Override public void onBackPressed() { moveTaskToBack(false); }
}
