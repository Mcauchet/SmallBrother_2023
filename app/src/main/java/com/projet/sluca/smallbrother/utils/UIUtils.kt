package com.projet.sluca.smallbrother.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData


/**
 * creates a toast with a message to print and vibrate
 *
 * @param [context] the context of the activity
 * @param [msg] the message to print
 * @param [vibreur] the phone Vibration system
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 26-12-22)
 */
fun message(context: Context, msg: String, vibreur: Vibration) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    vibreur.vibration(context, 300)
}

/**
 * Animation of loading
 *
 * @param [tvLoading] the TextView in which the animation takes place
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 15-12-2022)
 */
fun loading(tvLoading: TextView) {
    object : CountDownTimer(2000, 1) {
        override fun onTick(millisUntilFinished: Long) {
            when (millisUntilFinished) {
                in 1501..2000 -> tvLoading.text = ""
                in 1001..1500 -> tvLoading.text = "."
                in 501..1000 -> tvLoading.text = ".."
                in 0..500 -> tvLoading.text = "..."
            }
        }
        override fun onFinish(): Unit = loading(tvLoading)
    }.start()
}

/**
 * Set the log appearance
 * @param [userData] the user's data
 * @param [tvLog] the Log TextView
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun setLogAppearance(userData: UserData, tvLog: TextView) {
    check(userData.log != null)
    val sb = SpannableStringBuilder(userData.log)
    val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
    val bss = StyleSpan(Typeface.BOLD)
    sb.setSpan(fcs, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    sb.setSpan(bss, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    tvLog.text = sb
}

/**
 * Change the AppBarTitle according to the user's role
 * @param userData the data of the user
 * @param activity the activity running
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun setAppBarTitle(userData: UserData, activity: AppCompatActivity) {
    activity.supportActionBar?.title = if (userData.role == "Aidé") "SmallBrother - Aidé"
    else "SmallBrother - Aidant"
}