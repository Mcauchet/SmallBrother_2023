package com.projet.sluca.smallbrother

import android.Manifest
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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.models.UserData
import org.hamcrest.CoreMatchers.allOf
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
@LargeTest
class AidantActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AidantActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS
    )

    companion object {
        private lateinit var userData: UserData
        private lateinit var appContext: Context

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
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
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            userData.urlToFile = ""
            val file = File(appContext.filesDir, "SmallBrother/url.txt")
            if(file.exists()) file.delete()
            val dataFile = File(appContext.filesDir, "SmallBrother/donnees.txt")
            if(dataFile.exists()) dataFile.delete()
        }
    }


    @Before
    fun setUp() {
        Intents.init()
        userData.loadData(appContext)
    }

    @Test
    fun aidantActivityUITest() {
        onView(withId(R.id.btn_reglages)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_downloadFolder)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(withText("Appeler Jules")))
        onView(withId(R.id.btn_urgence)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_urgence)).check(matches(withText("Vérifier la situation de Jules")))
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
        Intents.intending(hasAction(Intent.ACTION_CALL)).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        onView(withId(R.id.btn_appel)).perform(click())
        onView(withId(R.id.log_texte))
            .check(matches(withSubstring("Vous appelez ${userData.nomPartner}.")))
        intended(allOf(
                hasAction(Intent.ACTION_CALL),
                hasData(Uri.parse("tel:" + userData.telephone))
        ))
    }

    @Test
    fun testDownloadFolderButton() {
        Intents.intending(hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        onView(withId(R.id.btn_downloadFolder)).perform(click())
        intended(hasAction(Intent.ACTION_VIEW))
    }

    @Test
    fun emergencyButtonTest() {
        onView(withId(R.id.btn_urgence)).perform(click())
        onView(withText(R.string.oui)).check(matches(isDisplayed()))
        onView(withText(R.string.oui)).perform(click())
        onView(withId(R.id.log_texte)).check(matches(withSubstring("Le traitement est en cours")))
    }

    @Test
    fun filesButtonTest() {
        val file = File(appContext.filesDir, "SmallBrother/url.txt")
        if(file.exists()) file.delete()
        file.createNewFile()
        onView(withId(R.id.btn_files)).check(matches(isDisplayed())).perform(click())
        assert(userData.urlToFile == "")
        assert(userData.bit == 7)
        Thread.sleep(300)
        onView(withId(R.id.log_texte)).check(matches(
            withSubstring("Il n'y a pas de fichier sur le serveur appartenant à Jules."))
        )
    }

    @Test
    fun urlSaveTest() {
        userData.saveURL(appContext, "abcdef")
        assert(userData.loadURL(appContext) == "abcdef")
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}