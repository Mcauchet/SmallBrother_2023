package com.projet.sluca.smallbrother;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.projet.sluca.smallbrother.Libs.AccelerometerListener;
import com.projet.sluca.smallbrother.Libs.AccelerometerManager;

import java.io.IOException;
import java.util.Arrays;

public class WorkActivity extends AppCompatActivity  implements SensorEventListener, AccelerometerListener
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.
    public TextView tvLoading;                   // Déclaration d'un objet TextView.
    public TextView tvAction;                    // Déclaration du TextView pour l'action en cours.

    public String clef;                          // Récupération d'un mot-clef reçu par SMS.
    public String appelant;                      // Récupération du numéro d'un appelant.

    public MediaRecorder magneto;                // Création d'un recorder audio.

    private float[] checkMove1;                  // Variables pour déterminer l'état de mouvement.
    private float[] checkMove2;
    private float[] keepMove;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml.
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

        // Déclaration d'un passage dans la WorkActivity pour éviter que, au retour dans
        // AideActivity, ne soit généré un doublon du Handler local.
        userdata.setEsquive(true);

        // Récupération d'un mot-clef reçu par SMS, s'il en est.
        if(SmsReceiver.catchClef() != null) clef = SmsReceiver.catchClef();

        // Récupération du numéro de l'appelant, suite à un appel reçu.
        if(PhoneStatReceiver.catchcallNumber() != null)
        {
            appelant = PhoneStatReceiver.catchcallNumber();
            appelant = appelant.replace("+32", "0");
            PhoneStatReceiver.resetCallNumber();
        }

        // SI APPEL RECU :

        if(appelant != "" && userdata.getTelephone().equals(appelant))
        {
                // Si l'appelant est bien le partenaire : màj du Log.
                if(userdata.getTelephone().equals(appelant)) userdata.refreshLog(8);

                retour(); // Retour à l'écran de rôle.
        }


        // SI SMS RECU : Lancement du traitement adéquat selon le mot-clef reçu :

        else if(clef != null)
        {
            switch(clef)
            {
                case "[#SB01]":  // -> Réinitialisation des données de l'Aidé.
                {
                    vibreur.vibration(context, 330);

                    userdata.byeData();        // Suppression des données de l'utilisateur.

                    userdata.refreshLog(3); // message de Log adéquat.

                    // Redémarrage de l'appli.
                    Intent mIntent = new Intent(context,Launch1Activity.class);
                    startActivity(mIntent);
                }
                break;

                case "[#SB02]":  // -> Réception d'un SMS "Tout va bien ?" par l'Aidé.
                {
                    userdata.refreshLog(6); // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidé.
                    Intent intent = new Intent(context, AideActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;

                case "[#SB03]":  // -> Réception d'un SMS "Oui, tout va bien" par l'Aidant.
                {
                    userdata.refreshLog(5); // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    Intent intent = new Intent(context, AidantActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;

                case "[#SB04]":  // -> Demande d'un mail d'urgence reçue par l'Aidé.
                {
                    // --> Vérification : l'appareil est bien connecté au Net.

                    if(checkInternet())
                    {
                        // Désactivation du SMSReceiver (pour éviter les cumuls de SMS).
                        PackageManager pm  = WorkActivity.this.getPackageManager();
                        ComponentName componentName = new ComponentName(WorkActivity.this, SmsReceiver.class);
                        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

                        loading(); // Déclenchement de l'animation de chargement.

                        // ================== [ Constitution du dossier joint ] ==================

                        // --> [1] captation et enregistrement d'un extrait sonore de dix secondes.
                        //     en parallèle se détermine également si le téléphone est en mouvement.

                        // Affichage de l'action en cours.
                        tvAction.setText(getString(R.string.message12A));

                        // Destination du futur fichier :
                        String fichier = userdata.getAudioPath();

                        // Configuration du recorder "magneto".
                        magneto = new MediaRecorder();
                        magneto.setAudioSource(MediaRecorder.AudioSource.MIC);
                        magneto.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        magneto.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        magneto.setOutputFile(fichier);

                        try { magneto.prepare(); }
                        catch (IOException e) { e.printStackTrace(); }

                        magneto.start(); // Enregistrement lancé.

                        // Délai de 10 secondes :
                        new CountDownTimer(10010, 1)
                        {
                            public void onTick(long millisUntilFinished)
                            {
                                // Aux secondes 9 et 2 sont capturé la position du téléphone.
                                     if(millisUntilFinished > 9000)
                                {
                                    checkMove1 = keepMove;
                                }
                                else if(millisUntilFinished < 2000 && millisUntilFinished > 1000)
                                {
                                    checkMove2 = keepMove;
                                }
                            }
                            public void onFinish() // Fin du délai :
                            {
                                // Conclusion de l'enregistrement.
                                magneto.stop();
                                magneto.release();
                                magneto = null;

                                // Déclaration : le téléphone est ou non en mouvement.
                                boolean suspens = Arrays.equals(checkMove1, checkMove2);
                                if(!suspens) userdata.setMotion(true);
                                else         userdata.setMotion(false);

                                // Suite des évènements dans une autre activity pour éviter les
                                // interférences entre les intents.
                                Intent intent = new Intent(context, Work2Activity.class);
                                startActivityForResult(intent, 1);
                            }
                        }.start();

                        // =======================================================================
                    }
                    else // Si pas de connexion :
                    {
                        userdata.refreshLog(12); // message de Log adéquat.

                        // Alarme : son et vibrations
                        MediaPlayer sound;
                        sound = MediaPlayer.create(this,R.raw.alarme );
                        sound.start();
                        vibreur.vibration(context, 5000);

                        // L'Aidant est averti par SMS de l'échec.
                        String sms = getString(R.string.smsys05);
                        sms = sms.replace("§%", userdata.getNom());
                        SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

                        // Retour à l'écran de rôle de l'Aidé.
                        Intent intent = new Intent(context, AideActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }
                break;

                case "[#SB05]":  // -> L'Aidant est avisé que l'Aidé n'a pas accès au Net.
                {
                    userdata.refreshLog(13); // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    Intent intent = new Intent(context, AidantActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;

                case "[#SB06]":  // -> L'Aidant est avisé que l'Aidé a clôt un envoi de mail.
                {
                    userdata.refreshLog(14); // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    Intent intent = new Intent(context, AidantActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;

                case "[#SB07]":  // -> L'Aidant est avisé que l'Aidé ne veut pas être dérangé.
                {
                    userdata.refreshLog(19); // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    Intent intent = new Intent(context, AidantActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;
            }
        }
    }


    // --> Animation des points de suspension en boucle de 2 secondes.
    public void loading()
    {
        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().setFlags
                (
                          WindowManager.LayoutParams.FLAG_FULLSCREEN       |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                          WindowManager.LayoutParams.FLAG_FULLSCREEN       |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );

        // Animation de chargement.
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


    // --> Retour à l'écran de rôle adéquat.
    public void retour()
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



    // Fonctions relatives à la consultation de l'accéléromètre (mouvement).

    @Override
    protected void onResume()
    {
        super.onResume();
        if (AccelerometerManager.isSupported(this) && userdata.getRole().equals("Aidé"))
        {
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z)
    {
        // Récupération des coordonnées de position du téléphone.
        // Imposition marge d'erreur (int val*10) pour contrer grande sensibilité capteurs.
        float[] tmp = { (int) x*10, (int) y*10, (int) z*10 };
        keepMove = tmp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) { }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onShake(float force) { }
}
