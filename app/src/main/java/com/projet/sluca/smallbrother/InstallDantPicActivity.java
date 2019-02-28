package com.projet.sluca.smallbrother;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import static android.graphics.Bitmap.CompressFormat.JPEG;

public class InstallDantPicActivity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.
    private ImageView apercu;                    // Instanciation de l'aperçu.
    private Intent intent;                       // Préparation à une activité parallèle.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_installdantpic.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installdantpic);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        // Identification de l'aperçu.
        apercu = (ImageView) findViewById(R.id.apercu);

        // Par défaut : afficher la photo enregistrée, s'il y en a une.
        String fichier = userdata.getPhotoIdentPath();
        File file      = new File( fichier );
        if(file.exists()) apercu.setImageURI(Uri.fromFile(file));
    }


    // --> Au clic que le bouton "Capture".
    public void capture(View view)
    {
        vibreur.vibration(context, 100);

        // Lancement de l'activité de capture.
        intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 7);
    }


    // --> Au retour à la présente actvité, si une photo a été prise :
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 7 && resultCode == RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");  // Récupération de la photo

            // -> Sauvegarde de la photo.
            String image = userdata.getPhotoIdentPath(); // chemin de fichier globalisé.
            try { bitmap.compress(JPEG, 100, new FileOutputStream(image)); }
            catch (FileNotFoundException e) {  e.printStackTrace(); }

            apercu.setImageBitmap(bitmap); // Affichage de la photo dans l'ImageView "aperçu".
        }
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

        // Rétablissement des boutons retour, au cas où désactivé par ReglagesActivity.
        userdata.whatAboutGoBack(true);

        // Transition vers l'activity suivante.
        Intent intent = new Intent(context, AidantActivity.class);
        startActivityForResult(intent, 1);
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
