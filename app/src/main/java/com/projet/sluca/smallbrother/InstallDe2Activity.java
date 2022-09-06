package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InstallDe2Activity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_installde.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installde2);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();
    }


    // --> Au clic que le bouton "Précédent".
    public void precedent(View view)
    {
        vibreur.vibration(context, 100);

        finish();
    }


    // --> Au clic que le bouton "Terminer".
    public void terminer(View view)
    {
        vibreur.vibration(context, 100);


        // > Récupération du contenu des inputs :

        // Email de l'Aidé :
        EditText et_myemail = (EditText) findViewById(R.id.input_mymail);
        String myemail = et_myemail.getText().toString();

        // Email :
        EditText et_password = (EditText) findViewById(R.id.input_password);
        String password = et_password.getText().toString();


        // > Vérification de la validité des informations entrées :

        // Vérification 1 : l'adresse email n'est pas valide.
        String provider = myemail.substring(myemail.length() - 10);
        if(!myemail.matches("") && !provider.equals("@gmail.com"))// !myemail.contains ("@"))
        {
            message(getString(R.string.error02));
        }
        // Vérification 2 : aucune entrée n'est vide.
        else if(myemail.matches("") || password.matches(""))
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
            userdata.setRole("Aidé");
            userdata.setMymail(myemail);
            userdata.setPassword(password);

            userdata.saveData(context); // Sauvegarde des données d'utilisateur.

            // Rétablissement des boutons retour, au cas où désactivé par ReglagesActivity.
            userdata.whatAboutGoBack(true);

            // Transition vers l'activity suivante.
            Intent intent = new Intent(context, AideActivity.class);
            startActivityForResult(intent, 1);
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
