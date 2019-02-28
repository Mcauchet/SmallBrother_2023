package com.projet.sluca.smallbrother;

import android.content.Context;
import android.os.Vibrator;

public class Vibration
{
    // --> VIBRATION() : vibration pour signifier un évènement.
    //     Prend en paramètre : le contexte de l'activité d'où elle est appelée, une durée en ms.
    public void vibration(Context context, int duree)
    {
        Vibrator shake = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        shake.vibrate(duree);
    }
}
