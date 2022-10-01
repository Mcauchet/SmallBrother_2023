package com.projet.sluca.smallbrother

import android.content.Context
import android.widget.Toast

//TODO test if this works as before, if it does collect every redundant functions and put them here
fun message(context: Context, msg: String, vibreur: Vibration) {
    val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
    toast.show()
    vibreur.vibration(context, 330)
}