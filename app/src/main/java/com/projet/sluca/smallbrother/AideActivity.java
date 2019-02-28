package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AideActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.
    private TextView tvLog;                      // Déclaration du TextView pour le Log.
    private TextView tvDelai;                    // Déclaration du TextView pour le délai.
    private TextView tvIntituleDelai;            // Déclaration du TextView pour l'intitulé du délai.
    private Switch btnDeranger;                  // Déclaration du bouton ON/OFF.
    private ImageView ivLogo;                    // Déclaration de l'ImageView du logo.
    private Handler logHandler;                  // Handler pour rafraîchissement log.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_aide.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();
        userdata.loadData();

        // Liaison avec les TextViews du délai.
        tvDelai         = (TextView) findViewById(R.id.decompte);
        tvIntituleDelai = (TextView) findViewById(R.id.intituleDecompte);

        // Liaison avec le switch ON/OFF et écoute de son état.
        btnDeranger = (Switch) findViewById(R.id.btn_deranger);
        btnDeranger.setOnCheckedChangeListener(this);

        // Liaison avec l'ImageView du logo.
        ivLogo = (ImageView) findViewById(R.id.logo);

        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        wakeup();

        // Rafraîchissement de l'affichage.
        refresh();

        // Réinitialisation de l'indicateur "Bit".
        SmsReceiver.setBit(0);

        // Liaison avec le TextView affichant le Log et ajout de sa valeur en cours.
        tvLog = (TextView) findViewById(R.id.log_texte);

        // Lancement de l'activité en arrière-plan (décompte) en évitant les doublons.
        /*
        if(this.logHandler == null ) // && !userdata.getEsquive())
        {
            this.logHandler = new Handler();
            reloadLog.run();
        }
        */
        this.logHandler = new Handler();
        reloadLog.run();
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


    // --> Au clic que le bouton "Réduire".
    public void reduire(View view)
    {
        vibreur.vibration(context, 200);

        message(getString(R.string.message01)); // Message d'avertissement.
        moveTaskToBack(true);          // Mise de l'appli en arrière-plan.
    }


    // --> Au clic que le bouton "SMS : Tout va bien ?".
    public void smsAide(View view)
    {
        vibreur.vibration(context, 200);

        userdata.loadData(); // Raptatriement des données de l'utilisateur.

        // Concoction et envoi du SMS.
        String sms = getString(R.string.smsys03);
        sms = sms.replace("§%", userdata.getNom());
        SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

        message(getString(R.string.message04)); // toast de confirmation.
        userdata.refreshLog(16);           // rafraîchissement du Log.
    }


    // --> Au clic que le bouton "Appel".
    public void appel(View view)
    {
        vibreur.vibration(context, 200);

        userdata.loadData(); // Raptatriement des données de l'utilisateur.

        // Balance contre l'interférence de l'Intent ci-dessous dans l'équilibre Work-Aide activity.
        //userdata.setEsquive(false);

        // Lancement de l'appel.
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + userdata.getTelephone()));
        startActivity(callIntent);

        message(getString(R.string.message05)); // toast de confirmation.
        userdata.refreshLog(7);           // rafraîchissement du Log.
    }


    // --> Traitement des postions ON/OFF du bouton "Mode Privé".
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(isChecked && !userdata.getPrive()) // Si "Mode Privé" demandé.
        {
            vibreur.vibration(context, 200);

            // Instanciaition d'une boîte de dialogue.
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.popup1, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            // Appel du layout "popup1" :
            alertDialogBuilder.setView(promptsView);

            // Récupération du contenu de l'input.
            final EditText input = (EditText) promptsView.findViewById(R.id.input_delai);

            // Affichage de la boîte de dialogue.
            alertDialogBuilder
                    .setCancelable(false)
                    // Si "Valider" :
                    .setPositiveButton(getString(R.string.btn_valider),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,int id)
                                {
                                    // Récupération du délai entré + sécurité si vaut null ou 0.
                                    Editable valinput;
                                    long valeur = 1;
                                    if(input.getText().toString().trim().length() != 0)
                                    {
                                        valinput = input.getText();
                                        String valnum = String.valueOf(valinput);
                                        if(Long.valueOf(valnum) == 0) valnum = "1";
                                        valeur = Long.valueOf(valnum);
                                    }

                                    // Délai max imposé.
                                    if(valeur > 120) valeur = 120;

                                    // Création du toast de confirmation.
                                    String duree = valeur + " minute";
                                    if(valeur > 1) duree += "s";
                                    String biscotte = getString(R.string.message10).replace("§%", duree);
                                    message(biscotte);

                                    // Détermination du délai.
                                    valeur *= 60000;
                                    userdata.setDelai(valeur);

                                    // Passage en Mode Privé.
                                    userdata.setPrive(true);
                                    SmsReceiver.setBit(1); // Cookie : Mode Privé ON.
                                    refresh();

                                    vibreur.vibration(context, 330);
                                }
                            })
                    // Si "Annuler" :
                    .setNegativeButton(getString(R.string.btn_annuler),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    vibreur.vibration(context, 100);

                                    changeSwitch(); // Changer la position du bouton.
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        if(!isChecked && userdata.getPrive()) // Si arrêt du "Mode Privé".
        {
            message(getString(R.string.message11)); // Toast de confirmation.
            userdata.setPrive(false);               // Arrêt du Mode Privé.
            SmsReceiver.setBit(0);                  // Cookie : Mode Privé OFF.
            userdata.setDelai(0);
            refresh();

            vibreur.vibration(context, 330);
        }
    }

    // -->
    public void changeSwitch()
    {
        vibreur.vibration(context, 250);
        refresh();
    }

    // --> Rafraîchissement de l'affichage en fonction de l'état du Mode Privé.
    public void refresh()
    {
        if(userdata.getPrive()) // Si actif.
        {
            btnDeranger.setTextColor(Color.parseColor("#b30000"));
            ivLogo.setImageResource(R.drawable.logoff);
            btnDeranger.setChecked(true);
        }
        else                    // Si inactif.
        {
            btnDeranger.setTextColor(Color.parseColor("#597854"));
            ivLogo.setImageResource(R.drawable.logo2);
            btnDeranger.setChecked(false);

            // Retrait du décompte.
            tvDelai.setText(" ");
            tvIntituleDelai.setText(" ");
        }
    }


    // --> Rafraîchissement automatique du TextView de Log toutes les 250 ms.
    private final Runnable reloadLog = new Runnable()
    {
        public void run()
        {
            // -> Vérification des actions parallèles (appels et sms reçus) :

            int bit = SmsReceiver.getBit();

            if(bit > 1)
            {
                vibreur.vibration(context, 660);

                if(bit == 2) // Cas : SMS "tout va bien" reçu.
                {
                    userdata.refreshLog( 6); // message de Log adéquat.
                }
                if(bit == 3) // Cas : appel reçu.
                {
                    userdata.refreshLog( 8); // message de Log adéquat.
                }
                if(bit == 4) // Cas : demande d'email d'urgence.
                {
                    userdata.refreshLog(12); // message de Log adéquat.

                    // Sonnerie de notification.
                    MediaPlayer sound;
                    sound = MediaPlayer.create(context,R.raw.notification);
                    sound.start();

                    // L'Aidant est averti par SMS de l'action du Mode Privé (+ reçoit tmp restant).
                    String sms = getString(R.string.smsys07);
                    sms = sms.replace("§%", userdata.getNom());
                    int restencore = ((int) (long) (userdata.getDelai() / 60000)) + 1;
                    String waitage = String.valueOf(restencore);
                    sms = sms.replace("N#", waitage);
                    SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);
                }

                tvLog.setText(userdata.getLog()); // affichage.

                SmsReceiver.setBit(1); // retour à décompte normal.
            }

            // -> Gestion du Log :

            if(userdata.getLog() != null)
            {
                // Coloration en vert et mise en gras de la date (19 premiers caras).
                final SpannableStringBuilder sb = new SpannableStringBuilder( userdata.getLog() );
                final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(57, 114, 26));
                final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sb.setSpan(bss, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                tvLog.setText(sb); // affichage
            }

            // -> Gestion du délai du Mode Privé :

            if(userdata.getPrive()) // Si le Mode Privé est actif.
            {
                userdata.subDelai(250); // délai moins le temps écoulé

                // Si le délai  est dépassé :
                if(userdata.getDelai() <= 0)
                {
                    wakeup(); // sortie de veille.

                    vibreur.vibration(context, 1000);

                    // Sonnerie de notification.
                    MediaPlayer sound;
                    sound = MediaPlayer.create(context,R.raw.notification);
                    sound.start();

                    // Retrait du décompte.
                    tvDelai.setText(" ");
                    tvIntituleDelai.setText(" ");

                    userdata.refreshLog(18); // rafraîchissement du Log.
                    userdata.setPrive(false);      // changement d'état.

                    // Reboot complet de l'activity.
                    Intent intent = new Intent(context, AideActivity.class);
                    startActivityForResult(intent, 1);
                }
                // Si le délai est toujours en cours :
                else
                {
                    // Mise à jour du décompte.

                    int min = ((int) (userdata.getDelai() / 60000));             // calcul des minutes
                    int sec = ((int) (userdata.getDelai() / 1000) - (min * 60)); // calcul des secondes
                    tvIntituleDelai.setText(getString(R.string.intitule_delai));
                    String secSTG = String.valueOf(sec);
                    if(sec < 10) secSTG = "0" + secSTG;
                    tvDelai.setText(" " + min + "\'" + secSTG);
                }
            }

            // Relance du Handler SI vérifications remplies pour éviter qu'il se duplique.
            if(!userdata.getEsquive() )AideActivity.this.logHandler.postDelayed(reloadLog,250); // rafraîchissement
            else userdata.setEsquive(false);
        }
    };


    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    public void message(String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        vibreur.vibration(context, 330);
    }


    // --> WAKEUP() : Sortie de veille du téléphone et mise en avant-plan de cette appli.
    public void wakeup()
    {
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
    }


    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    @Override public void onBackPressed() { moveTaskToBack(false); }
}
