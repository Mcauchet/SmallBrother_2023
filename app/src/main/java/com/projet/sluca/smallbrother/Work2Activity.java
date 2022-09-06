package com.projet.sluca.smallbrother;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.widget.TextView;
import com.projet.sluca.smallbrother.Libs.APictureCapturingService;
import com.projet.sluca.smallbrother.Libs.Compress;
import com.projet.sluca.smallbrother.Libs.PictureCapturingListener;
import com.projet.sluca.smallbrother.Libs.PictureCapturingServiceImpl;
import com.projet.sluca.smallbrother.Libs.Sender;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

public class Work2Activity extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.
    public TextView tvLoading;                   // Déclaration d'un objet TextView.
    public TextView tvAction;                    // Déclaration du TextView pour l'action en cours.

    public String urlGoogleMap;                 // Retiendra l'url vers la carte avec positionnement.
    private String batterie;                     // Retiendra le niveau de batterie restant.

    // Attribut de permission pour l'appel aux méthodes de "APictureCapturingService".
    private APictureCapturingService pictureService;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml (même écran).
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        context = this; // Mise à jour de l'état.

        // Liaison et remplissage des objets TextView.
        tvLoading = (TextView) findViewById(R.id.loading);
        tvAction  = (TextView) findViewById(R.id.action);
        tvLoading.setText("");
        tvAction.setText("");

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        loading(); // Déclenchement de l'animation de chargement.

        // ================== [ Constitution du dossier joint ] ==================

        // --> [2] prise de deux photos automatiquement.

        // Affichage de l'action en cours.
        tvAction.setText(getString(R.string.message12B));

        // Lancement de la capture.
        pictureService = PictureCapturingServiceImpl.getInstance(this);
        pictureService.startCapturing(this);
    }

    // Suite du processus après que les photos soient prises :

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken)
    {
        // --> [3] localisation de l'Aidé.

        // Affichage de l'action en cours.
        tvAction.setText(getString(R.string.message12C));

        String latitude;  // Retiendra la coordonnée de latitiude.
        String longitude; // Retiendra la coordonnée de longitude.

        // Vérification obligatoire des permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)  { return; }

        // Récupération des données de latitude et longitude de l'appareil.
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location != null)
        {
            // Mémorisation des coordonnées récupérées.
            latitude  = String.valueOf(location.getLatitude());  // latitude
            longitude = String.valueOf(location.getLongitude()); // longitude

            // Construction de l'URL GoogleMap avec les coordonnées.
            urlGoogleMap = "http://maps.google.com/maps?q=" + latitude + "," + longitude;
        }

        // --> [4] assemblage d'une archive ZIP.

        // Affichage de l'action en cours.
        tvAction.setText(getString(R.string.message12D));

        // Récupération des différents fichiers :

        String []paquet = new String[3];  // liste des fichiers à zipper.
        int numCell = 0;                  // marqueur numérique incrémentable.

        String fichier1 = userdata.getAudioPath();
        File file1      = new File( fichier1 );

        if(file1.exists()) // Enregistrement audio.
        {
            paquet[numCell] = fichier1;
            numCell++;
        }

        String fichier2 = userdata.getAutophotosPath(1);
        File file2      = new File( fichier2 );

        if(file2.exists()) // Autophoto 1.
        {
            paquet[numCell] = fichier2;
            numCell++;
        }

        String fichier3 = userdata.getAutophotosPath(2);
        File file3      = new File( fichier3 );

        if(file3.exists()) // Autophoto 2.
        {
            paquet[numCell] = fichier3;
        }

        // Chemin de la future archive.
        String ziPath = userdata.getZipath();

        // Lancement de la compression.
        Compress c = new Compress( paquet , ziPath );
        c.zip();

        // --> [5] niveau de batterie.

        // Affichage de l'action en cours.
        tvAction.setText(getString(R.string.message12E));

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        batterie = String.valueOf(level) + "%";

        // --> [6] déterminer si en mouvement.

        String                   motion = "Non";
        if(userdata.getMotion()) motion = "Oui";

        // --> [7] envoi de l'email d'urgence (avec Libs/Sender.java).

        // Affichage de l'action en cours.
        tvAction.setText(getString(R.string.message12F));

        // Métadonnées du mail.
        final String login = userdata.getMymail();
        final String pwd   = userdata.getPassword();
        String objet = "Situation de " + userdata.getNom();
        String email = userdata.getEmail();

        // Détermine la synthaxe du message selon la première lettre du nom de l'Aidé.
        String nomAide = userdata.getNom();
        String particule = Character.toString(nomAide.charAt(0));
        String[] voyelles = { "A","E","Y","U","I","O","É","È","Œ","a","e","y","u","i","o","é","è" };
        if(Arrays.asList( voyelles ).contains( particule )) particule = " d'";
        else                                                particule = " de ";

        String url = " <a href=\"" + urlGoogleMap + "\">ouvrir dans GoogleMap</a>";
        String message =
                "<br><b>Localisation " + particule + nomAide + " :</b> " + url +
                "<br><br>" +
                "<b>Niveau de batterie :</b> " + batterie +
                "<br><br>" +
                "<b>En mouvement :</b> " + motion +
                "<br><br>";

        new Thread()
        {
            public void run()
            {
                try
                {
                    // Création d'un Sender connecté au compte Gmail.
                    Sender sender = new Sender
                            (
                                    login,  // Login Gmail
                                    pwd     // Mdp Gmail
                            );

                    // Ajout des données du mail dans le Sender.
                    sender.sendMail
                            (
                                    objet,    // Objet
                                    message,  // Contenu du message
                                    login,    // Envoyeur
                                    email     // Destinataire
                            );
                }
                catch (Exception e) { }

                // Suppression des captures.
                file1.delete();
                file2.delete();
                file3.delete();

                // Suppression du fichier ZIP.
                File fileZ = new File( ziPath );
                fileZ.delete();

                // Rafraîchissement du Log en fonction de la réussite du processus.
                if(checkInternet()) userdata.refreshLog(11); // réussi.
                else                userdata.refreshLog(15); // coupure Internet entretemps.

                // Concoction et envoi du SMS à l'Aidant.
                String sms = getString(R.string.smsys06);
                sms = sms.replace("§%", userdata.getNom());
                SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

                vibreur.vibration(context, 330); // vibration.

                // Réactivation du SmsReceiver.
                PackageManager pm  = Work2Activity.this.getPackageManager();
                ComponentName componentName = new ComponentName(Work2Activity.this, SmsReceiver.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                // Retour à l'écran de rôle de l'Aidé.
                Intent intent = new Intent(context, AideActivity.class);
                startActivityForResult(intent, 1);
            }
        }.start(); // Envoi !

        // =======================================================================
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) { }


    // --> Animation des points de suspension en boucle de 2 secondes.
    public void loading()
    {
        new CountDownTimer(2000, 1)
        {
            public void onTick(long millisUntilFinished)
            {
                // A chaque seconde passée, modifier le contenu l'objet TextView.
                if(millisUntilFinished > 1600) tvLoading.setText(   "");
                else if(millisUntilFinished > 1200) tvLoading.setText(  ".");
                else if(millisUntilFinished >  800) tvLoading.setText( "..");
                else if(millisUntilFinished >  400) tvLoading.setText("...");
            }
            public void onFinish()
            {
                loading();
            }
        }.start();
    }


    // --> CHECKINTERNET() : Renvoie vrai si l'appareil est connecté au Net.
    private boolean checkInternet()
    {
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }


    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    @Override public void onBackPressed() { moveTaskToBack(false); }
}
