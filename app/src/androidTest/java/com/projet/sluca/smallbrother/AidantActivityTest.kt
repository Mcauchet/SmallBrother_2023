package com.projet.sluca.smallbrother

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.models.UserData
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class AidantActivityTest {

    private lateinit var userData: UserData
    private lateinit var appContext: Context

    @get:Rule
    val activityRule = ActivityScenarioRule(AidantActivity::class.java)

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
        if(file.exists()) file.delete()
        userData = UserDataManager.getUserData(appContext.applicationContext as Application)
        userData.version = "1.2"
        userData.role = "Aidant"
        userData.nom = "Émilie"
        userData.telephone = "0476546545"
        userData.pubKey = "FakePublicKey"
        userData.nomPartner = "Jules"
        userData.path = appContext.filesDir.path
        userData.prive = false
        userData.bit = 0
        userData.saveData(appContext)
        activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }
        Intents.init()

        println(userData.role)
        println(userData.nomPartner)
    }

    @Test
    fun aidantActivityUITest() {
        onView(withId(R.id.btn_reglages)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_reduire)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(withText("Appeler Jules")))
        onView(withId(R.id.btn_urgence)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_urgence)).check(matches(withText("Capturer le contexte de Jules")))
        onView(withId(R.id.btn_files)).check(matches(isDisplayed()))
    }

    @Test
    fun settingsButtonTest() {
        onView(withId(R.id.btn_reglages)).perform(click())
        onView(withId(R.id.btn_reinit_1)).check(matches(isDisplayed()))
    }

    @Test
    fun pictureButtonTest() {
        onView(withId(R.id.btn_photo)).perform(click())
        onView(withId(R.id.legende)).check(matches(isDisplayed()))
    }

    @Test
    fun smsButtonTest() {
        onView(withId(R.id.btn_sms_va_dant)).perform(click())
        assert(userData.bit == 0)
        onView(withId(R.id.log_texte)).check(matches(withSubstring("SMS envoyé")))
    }

    @Test
    fun callButtonTest() {
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CALL)).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        onView(withId(R.id.btn_appel)).perform(click())
        onView(withId(R.id.log_texte))
            .check(matches(withSubstring("Vous appelez ${userData.nomPartner}.")))
        intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CALL),
                IntentMatchers.hasData(Uri.parse("tel:" + userData.telephone))
        ))
    }

    @Test
    fun emergencyButtonTest() {
        onView(withText("Oui")).perform(click())
        onView(withId(R.id.log_texte)).check(matches(withSubstring("Le traitement est en cours")))
    }

    @Test
    fun filesButtonTest() {
        val file = File(appContext.filesDir, "SmallBrother/url.txt")
        if(file.exists()) file.delete()
        file.createNewFile()
        file.writeText("abcdef")
        onView(withId(R.id.btn_files)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_files)).perform(click())
        assert(userData.urlToFile == "abcdef")
    }

    @Test
    fun urlSaveTest() {
        userData.saveURL(appContext, "abcdef")
        assert(userData.loadURL(appContext) == "abcdef")
    }

    @After
    fun tearDown() {
        userData.urlToFile = ""
        val file = File(appContext.filesDir, "SmallBrother/url.txt")
        if(file.exists()) file.delete()
        val dataFile = File(appContext.filesDir, "SmallBrother/donnees.txt")
        if(dataFile.exists()) dataFile.delete()
        Intents.release()
    }
}