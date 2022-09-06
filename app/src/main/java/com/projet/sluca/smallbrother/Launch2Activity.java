package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class Launch2Activity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_launch2.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch2);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();
    }


    // --> Au clic que le bouton "Aidant".
    public void aidant(View view)
    {
        vibreur.vibration(context, 100);

        userdata.setRole("Aidant");

        // Transition vers l'activity suivante.
        Intent intent = new Intent(context, InstallDantActivity.class);
        startActivityForResult(intent, 1);
    }


    // --> Au clic que le bouton "Aidé".
    public void aide(View view)
    {
        vibreur.vibration(context, 100);

        userdata.setRole("Aidé");

        // Transition vers l'activity suivante.
        Intent intent = new Intent(context, InstallDeActivity.class);
        startActivityForResult(intent, 1);
    }
}
