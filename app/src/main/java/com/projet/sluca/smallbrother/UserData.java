package com.projet.sluca.smallbrother;

// Mémorisation et gestion des données globales de l'utilisateur et autres données utiles, en session et en DB.

import android.app.Application;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserData extends Application
{
    // Attributs

    public String version;      // Dernière version de SB installée.
    public String role;         // Rôle de l'utilisateur.
    public String nom;          // Nom de l'utilisateur.
    public String telephone;    // Numéro de l'utilisateur.
    public String email;        // Email de l'Aidant.
    public String mymail;       // Email de l'Aidé.
    public String password;     // Mot de passe de l'utilisateur.

    public boolean motion;      // L'utilisateur est, ou non, en mouvement.

    public boolean prive;       // Etat du bouton ON/OFF "ne pas déranger".
    public long delai;          // Retiendra le temps passé en mode "ne pas déranger".
    public boolean esquive;     // Manoeuvre pour éviter les doublons de handlers pour l'AidéActivity.

    public String log;          // Contenu du Log.
    public boolean canGoBack;   // Indique si retour arrière possible ou non.

    // Centralisation des chemins de fichiers :
    private String path  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/SmallBrother/";
    private String file  = "donnees.txt";    // datas de l'utilisateur
    private String fiche = "fiche_aide.txt"; // fiche de l'Aidé
    private String date  = "date.txt";       // date de création de la fiche de l'Aidé
    private String photo = "photo_aide.jpg"; // photo de l'Aidé
    private String zipath = "/sdcard/Download/SmallBrother/situation_partenaire.zip";

    // Centralisation des URL :
    private String url  = "https://projects.info.unamur.be/geras/projects/smallbrother/";
    private String help = "help/";


    // Getters

    public String getVersion()   { return version;   }
    public String getRole()      { return role;      }
    public String getNom()       { return nom;       }
    public String getTelephone() { return telephone; }
    public String getEmail()     { return email;     }
    public String getMymail()    { return mymail;    }
    public String getPassword()  { return password;  }

    public boolean getMotion()    { return motion;   }

    public boolean getPrive()    { return prive;     }
    public long getDelai()       { return delai;     }
    public boolean getEsquive()   { return esquive;  }

    public String getLog()       { return log;       }
    public boolean canIGoBack()  { return canGoBack; }


    // Setters

    public void setVersion(String version)      { this.version   = version;   }
    public void setRole(String role)            { this.role      = role;      }
    public void setNom(String nom)              { this.nom       = nom;       }
    public void setTelephone(String telephone)  { this.telephone = telephone; }
    public void setEmail(String email)          { this.email     = email;     }
    public void setMymail(String mymail)        { this.mymail    = mymail;    }
    public void setPassword(String password)    { this.password  = password;  }

    public void setMotion(boolean motion)       { this.motion    = motion;    }

    public void setPrive(boolean about)         { this.prive     =  about;    }
    public void setDelai(long time)             { this.delai     =  time;     }
    public void setEsquive(boolean esquive)     { this.esquive   = esquive;   }

    public void setLog(String log)              { this.log       = log;       }
    public void whatAboutGoBack(boolean about)  { this.canGoBack = about;     }


    // Fonctions complexes :

    // -> Sauvegarde en TXT des données de l'utilisateur.
    public void saveData(Context context)
    {
        // Structuration du contenu du futur fichier (info, retour-charriot).
        String contenu = this.version + "\r" + this.role + "\r" + this.nom + "\r" + this.telephone;

        if(this.email != null) // Si compte de l'Aidé :
        {
            contenu += "\r" + this.email + "\r" + this.mymail + "\r" + this.password;
        }

        // Enregistrement :
        try
        {
            // Création du dossier "Downloads/SmallBrother", s'il n'existe pas déjà.
            File dossier = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "SmallBrother");
            if(!dossier.exists()) { dossier.mkdirs(); }

            // Création du fichier "donnees.txt" dans ce dossier, via la variable "path".
            File testFile = new File( path + file );
            if (!testFile.exists()) testFile.createNewFile();
            else
            {
                byeData(); // Suppression du fichier de données s'il existe déjà.
            }

            // Ecriture.
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true));
            writer.write(contenu);
            writer.close();
            MediaScannerConnection.scanFile(context, new String[]{ testFile.toString() }, null,null);
        }
        catch (IOException e){ }
    }


    // -> Suppression des données de l'utilisateur.
    public void byeData()
    {
        File data = new File( path + file );
        data.delete();
    }


    // -> Chargement du fichier TXT contenant les données de l'utilisateur et conversion en session.
    public boolean loadData()
    {
        // Chargement du fichier TXT pointé par "path".
        File data = new File( path + file );

        if (data.exists())
        {
            try // Récupération du contenu du fichier :
            {
                // Placement des données dans un array, séparation par le retour-charriot.
                BufferedReader br = new BufferedReader(new FileReader( data ));
                String dataLine   = org.apache.commons.io.IOUtils.toString( br );
                String[] dataTab  = dataLine.split("\r");

                // Rapatriement des données :
                setVersion(dataTab[0]);
                setRole(dataTab[1]);
                setNom(dataTab[2]);
                setTelephone(dataTab[3]);
                if(dataTab.length > 4 && dataTab[4] != null) // Si compte de l'Aidé :
                {
                    setEmail(dataTab[4]);    // + email de l'Aidant
                    setMymail(dataTab[5]);   // + email de l'Aidé
                    setPassword(dataTab[6]); // + son mdp
                }

                return true;
            }
            catch (FileNotFoundException e) { e.printStackTrace(); }
            catch (IOException e)           { e.printStackTrace(); }
        }
        return false;
    }


    // -> Appel du chemin globalisé vers le dossier "SmallBrother".
    public String getPath() { return path; }

    // -> Appel du chemin globalisé vers la photo d'identité de l'aidé.
    public String getPhotoIdentPath() { return path + photo; }

    // -> Appel du chemin globalisé vers la capture audio.
    public String getAudioPath() { return path + "audio.ogg"; }

    // -> Appel du chemin globalisé vers les photos capturées (1 et 2).
    public String getAutophotosPath(int num)
    {
        return path + "autophoto" + String.valueOf(num) + ".jpg";
    }

    // -> Appel du chemin globalisé vers la fiche de l'aidé.
    public String getFichePath() { return path + fiche; }

    // -> Appel du chemin globalisé vers le zip d'un rapport de situation.
    public String getZipath() { return zipath; }

    // -> Donne l'URL de la racine du dossier Web de SB.
    public String getUrl() { return url; }

    // -> Donne la part d'URL nécessaire pour accéder à l'aide de SB.
    public String getHelp() { return help; }


    // -> Mise à jour du Log en fonction du numéro entré en paramètre.
    public void refreshLog(int code)
    {
        // Capture de la date et de l'heure actuelles.
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        String date = df.format(c.getTime());

        String texte =  date + " : "; // Début de message.

        // Ajout de la notification (res/values/strings) relative au paramètre entré :
        switch(code)
        {
            case  1: texte += getString(R.string.log01); break;
            case  2: texte += getString(R.string.log02); break;
            case  3: texte += getString(R.string.log03); break;
            case  4: texte += getString(R.string.log04); break;
            case  5: texte += getString(R.string.log05); break;
            case  6: texte += getString(R.string.log06); break;
            case  7: texte += getString(R.string.log07); break;
            case  8: texte += getString(R.string.log08); break;
            case  9: texte += getString(R.string.log09); break;
            case 10: texte += getString(R.string.log10); break;
            case 11: texte += getString(R.string.log11); break;
            case 12: texte += getString(R.string.log12); break;
            case 13: texte += getString(R.string.log13); break;
            case 14: texte += getString(R.string.log14); break;
            case 15: texte += getString(R.string.log15); break;
            case 16: texte += getString(R.string.log16); break;
            case 17: texte += getString(R.string.log17); break;
            case 18: texte += getString(R.string.log18); break;

            case 19:
                // Message avec insertion du temps de Mode Privé restant.
                texte += getString(R.string.log19);
                texte = texte.replace("N#", SmsReceiver.catchTempsRestant());
                break;
        }

        setLog(texte); // Set du Log.
    }

    // -> Retrait d'une certaine quantité de temps au délai gardé en mémoire.
    public void subDelai(long sub) { this.delai -= sub; } // Délai moins le paramètre.


    // -> Création de la fiche de l'Aidé.
    public void createFiche(Context context)
    {
        String texte = "";  // Futur contenu du fichier texte.

        String[] champs =   // Liste des champs du fichier.
        {
            "Concerne : Mr/Mme ... ",
            "Ses centres d'intérêt : ",
            "Ses médicaments : ",
            "Son médecin traitant : ",
            "Sa famille se compose de : ",
            "Ses handicaps : ",
            "Ses allergies : ",
            "Capable de solliciter de l’aide à des passants ? : ",
            "La personne de confiance à joindre : "
        };

        // Assemblage du contenu en un String.
        for (String ligne : champs)
        {
            texte += ligne + "\r\r";
        }

        // Enregistrement de la fiche :
        File fichette = new File( path + fiche );
        writeFile(fichette, texte, context);

        try  { Runtime.getRuntime().exec("chmod 777 " + path + fiche); }
        catch (IOException e) { e.printStackTrace(); }

        // Mémorisation de la date de création de la fiche :
        File datation = new File( path + date );
        writeFile(datation, getToDayte(), context);
    }

    // -> Centralisation de l'écriture de fichier
    public void writeFile(File file, String texte, Context context)
    {
        try
        {
            if (!file.exists()) // Créer uniquement si non déjà existant.
            {
                file.createNewFile();

                // Ecriture.
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.write( texte );
                writer.close();
                MediaScannerConnection.scanFile(context, new String[]{ file.toString() }, null,null);
            }
        }
        catch (IOException e){ }
    }

    public String getToDayte()
    {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    // -> Récupère la date de modification d'un fichier
    public String dateFichier(String chemin)
    {
        File file = new File( chemin );
        Date lastModDate = new Date(file.lastModified());

        return lastModDate.toString();
    }

    // -> Vérification : la fiche de l'Aidé existe et a été complétée.
    public boolean pleineFiche()
    {
        // Chargement du fichier TXT retenant la date de création de la fiche de l'Aidé.
        File dataD = new File( path + date );
        File dataF = new File( path + fiche );

        if (dataD.exists() && dataF.exists())
        {
            if(!dateFichier(path + date).equals(dateFichier(path + fiche)))
            {
                return true;
            }
        }
        return false;
    }
}
