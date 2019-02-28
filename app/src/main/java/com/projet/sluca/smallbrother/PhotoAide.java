package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

// --> Affichage de la photo de l'Aidé.

public class PhotoAide extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.

    // Eléments d'affichage (photo et légende);
    public ImageView ivApercu;
    public TextView tvLegende;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_photo.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        // Identification des éléments d'affichage.
        ivApercu  = (ImageView) findViewById(R.id.apercu);
        tvLegende = (TextView) findViewById(R.id.legende);

        // Gestion de l'affichage, selon qu'un fichier existe ou non.
        String fichier = userdata.getPhotoIdentPath();
        File file      = new File( fichier );
        if(file.exists()) ivApercu.setImageURI(Uri.fromFile(file));
        else tvLegende.setText(getString(R.string.nophoto));
    }


    // --> Au clic que le bouton "Retour".
    public void retour(View view)
    {
        vibreur.vibration(context, 100);

        // Transition vers la AidantActivity.
        Intent intent = new Intent(context, AidantActivity.class);
        startActivityForResult(intent, 1);
    }
}
