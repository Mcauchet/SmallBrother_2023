package com.projet.sluca.smallbrother;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class AidantActivity extends AppCompatActivity
{
    private Context context;                     // Définira l'état courant de l'appli.
    public Vibration vibreur = new Vibration();  // Instanciation d'un vibreur.
    public UserData userdata = new UserData();   // Liaison avec les données globales de l'utilisateur.
    private TextView tvLog;                      // Déclaration du TextView pour le Log.
    private Handler logHandler;                  // Handler pour rafraîchissement log.
    private FrameLayout flTiers;                 // Déclaration du FrameLayout pour le bouton Tiers.


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Etablissement de la liaison avec la vue res/layout/activity_aidant.xml.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidant);
        context = this; // Mise à jour de l'état.

        // Etablissement de la liaison avec la classe UserData.
        userdata = (UserData) getApplication();

        // Liaison avec le TextView affichant le Log et ajout de sa valeur en cours.
        tvLog = (TextView) findViewById(R.id.log_texte);

        // Liaison avec le FrameLayout affichant le bouton Tiers.
        flTiers = (FrameLayout) findViewById(R.id.contour4);

        // Lancement de l'activité en arrière-plan (rafraîchissement).
        this.logHandler = new Handler();
        reloadLog.run();

        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        wakeup();
    }


    // --> Au clic que le bouton "Réduire".
    public void reduire(View view)
    {
        vibreur.vibration(context, 200);

        message(getString(R.string.message01)); // Message d'avertissement.
        moveTaskToBack(true);          // Mise de l'appli en arrière-plan.
    }


    // --> Au clic que le bouton "Réglages".
    public void reglages(View view)
    {
        vibreur.vibration(context, 100);

        // Transition vers la ReglagesActivity.
        Intent intent = new Intent(context, ReglagesActivity.class);
        startActivityForResult(intent, 1);
    }

    // --> Au clic que le bouton "Photo".
    public void photo(View view)
    {
        vibreur.vibration(context, 100);

        // Transition vers la ReglagesActivity.
        Intent intent = new Intent(context, PhotoAide.class);
        startActivityForResult(intent, 1);
    }


    // --> Au clic que le bouton "SMS : Tout va bien ?".
    public void smsAidant(View view)
    {
        vibreur.vibration(context, 200);

        userdata.loadData(); // Raptatriement des données de l'utilisateur.

        // Concoction et envoi du SMS.
        String sms = getString(R.string.smsys02);
        sms = sms.replace("§%", userdata.getNom());
        SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

        message(getString(R.string.message04)); // toast de confirmation.
        userdata.refreshLog(4);           // rafraîchissement du Log.
    }


    // --> Au clic que le bouton "Appel".
    public void appel(View view)
    {
        vibreur.vibration(context, 200);

        userdata.loadData(); // Raptatriement des données de l'utilisateur.

        // Lancement de l'appel.
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + userdata.getTelephone()));
        startActivity(callIntent);

        message(getString(R.string.message05)); // toast de confirmation.
        userdata.refreshLog(7);           // rafraîchissement du Log.
    }

    // --> Au clic que le bouton "Demander un email d'urgence".
    public void urgence(View view)
    {
        vibreur.vibration(context, 330);

        // Demande de confirmation.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(true);
        builder.setTitle(getString(R.string.btn_urgence));
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
                String sms = getString(R.string.smsys04);
                sms = sms.replace("§%", userdata.getNom());
                SmsManager.getDefault().sendTextMessage(userdata.getTelephone(), null, sms, null,null);

                message(getString(R.string.message07)); // toast de confirmation.
                userdata.refreshLog(10);          // rafraîchissement du Log.
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


    // --> Au clic que le bouton "Envoyer les infos à ...".
    public void tiers(View view)
    {
        vibreur.vibration(context, 200);

        userdata.loadData(); // Raptatriement des données de l'utilisateur.

        // Préparation d'un email avec fichier joint.
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("plain/text");
        ArrayList<Uri> listUri=new ArrayList<Uri>();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Ajout de la fiche de l'Aidé.
        Uri URI = Uri.fromFile(new File(userdata.getFichePath()));
        listUri.add(URI);

        // Ajout de la photo de l'Aidé, s'il y en a une.
        File photoident = new File( userdata.getPhotoIdentPath() );
        if(photoident.exists())
        {
            Uri URI2 = Uri.fromFile(new File(userdata.getPhotoIdentPath()));
            listUri.add(URI2);
        }

        // Appel du choix des services mail disponibles.
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri);
        startActivity(Intent.createChooser(emailIntent,"Quel service email utiliser ?"));
    }


    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    public void message(String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        vibreur.vibration(context, 330);
    }


    // --> Rafraîchissement automatique toutes les 250 ms du TextView de Log et des boutons.
    private final Runnable reloadLog = new Runnable()
    {
        public void run()
        {
            // Log :
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

            // Bouton Tiers :
            if(userdata.pleineFiche())
            {
                flTiers.setVisibility(View.VISIBLE);
            }
            else
            {
                flTiers.setVisibility(View.GONE);
            }

            AidantActivity.this.logHandler.postDelayed(reloadLog,250); // rafraîchissement
        }
    };


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
