package com.projet.sluca.smallbrother;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Launch1Activity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_launch1.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch1);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        vibreur.vibration(context, 100);

        // Réactivation du SmsReceiver (en cas de coupure inopinée de l'appli).
        PackageManager pm  = Launch1Activity.this.getPackageManager();
        ComponentName componentName = new ComponentName(Launch1Activity.this, SmsReceiver.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // Vérification : tout premier démarrage ?

        // Cas 1 : data existant : redirection vers l'écran de rôle.
        if(userdata.loadData())
        {
            if(userdata.getRole().equals("Aidant"))
            {
                // Envoie vers l'installation d'un Aidant.
                Intent intent = new Intent(context, AidantActivity.class);
                startActivityForResult(intent, 1);
            }
            else if(userdata.getRole().equals("Aidé"))
            {
                // Envoie vers l'installation d'un Aidé.
                Intent intent = new Intent(context, AideActivity.class);
                startActivityForResult(intent, 1);
            }
        }
        // Cas 2 : data inexistant mais un rôle en session, donc réinitialisation.
        else if(userdata.getRole() != null)
        {
            // Désactivation des boutons retour (car suite de Reglages Activity).
            userdata.whatAboutGoBack(false);

            userdata.refreshLog(2);   // message de Log adéquat.

            if(userdata.getRole().equals("Aidant"))
            {
                // Envoie vers l'installation d'un Aidant.
                Intent intent = new Intent(context, InstallDantActivity.class);
                startActivityForResult(intent, 1);
            }
            else if(userdata.getRole().equals("Aidé"))
            {
                // Envoie vers l'installation d'un Aidé.
                Intent intent = new Intent(context, InstallDeActivity.class);
                startActivityForResult(intent, 1);
            }
        }
        // Cas 3 : première installation
        else
        {
            userdata.whatAboutGoBack(true); // activation des boutons retour.
            userdata.refreshLog(1);   // message de Log de commencement.
        }
    }


    // --> Au clic que le bouton "Commencer".
    public void commencer(View view)
    {
        vibreur.vibration(context, 100);

        // Transition vers l'activity suivante.
        Intent intent = new Intent(context, Launch2Activity.class);
        startActivityForResult(intent, 1);
    }
}
