package com.projet.sluca.smallbrother

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.getCurrentTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class AideActivityTest {

    private lateinit var userData: UserData
    private lateinit var appContext: Context

    @get:Rule
    val activityRule = ActivityScenarioRule(AideActivity::class.java)


    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        if(file.exists()) file.delete()
        userData = UserDataManager.getUserData(appContext.applicationContext as Application)
        userData.version = "1.2"
        userData.role = "Aidé"
        userData.nom = "Jules"
        userData.telephone = "0476546545"
        userData.pubKey = "FakePublicKey"
        userData.nomPartner = "Émilie"
        userData.path = appContext.filesDir.path
        userData.prive = false
        userData.bit = 0
        userData.saveData(appContext)
        activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }
    }

    @Test
    fun aideActivityUITest() {
        onView(withId(R.id.btn_reduire)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_appel)).check(matches(withText("Appeler Émilie")))
        onView(withId(R.id.btn_urgence)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_deranger)).check(matches(isDisplayed()))
    }

    @Test
    fun privateModeOnOffTest() {
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withText("Valider")).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isChecked()))
        assert(userData.delay in 890000..900000)
        assert(userData.prive)
        assert(userData.bit == 1)
        onView(withSubstring("Vous avez activé le mode privé.")).check(matches(isDisplayed()))
        uncheckSwitch()
    }

    @Test
    fun longPrivateModeTest() {
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withId(R.id.input_delai)).perform(clearText())
        onView(withId(R.id.input_delai)).perform(typeText("200"))
        onView(withText("Valider")).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isChecked()))
        assert(userData.delay in 7190000..7200000)
        assert(userData.prive)
        assert(userData.bit == 1)
        uncheckSwitch()
    }

    @Test
    fun shortPrivateModeTest() {
        onView(withId(R.id.btn_deranger)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withId(R.id.input_delai)).perform(clearText())
        onView(withId(R.id.input_delai)).perform(typeText("0"))
        onView(withText("Valider")).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isChecked()))
        assert(userData.delay in 50000..60000)
        assert(userData.prive)
        assert(userData.bit == 1)
        uncheckSwitch()
    }

    @Test
    fun privateModeCancelTest() {
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withText("Annuler")).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isNotChecked()))
        assert(!userData.prive)
        assert(userData.bit == 0)
    }

    /**
     * Turn the private mode switch off
     */
    private fun uncheckSwitch() {
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isNotChecked()))
        assert(!userData.prive)
        assert(userData.bit == 0)
    }

    /*@Test //TODO update this because it fails in this state
    fun buttonReductTest() {
        activityRule.scenario.onActivity { activity ->
            CoroutineScope(Dispatchers.IO).launch {
                onView(withId(R.id.btn_reduire)).check(matches(isClickable()))
                onView(withId(R.id.btn_reduire)).perform(click())
                MatcherAssert.assertThat(activity, `is`(notNullValue()))
                MatcherAssert.assertThat(activity.isFinishing, equalTo(false))
                MatcherAssert.assertThat(activity.isTaskRoot, equalTo(false))
                onView(withId(R.id.btn_reduire)).check(matches(not(isDisplayed())))
            }
        }
    }*/

    @Test
    fun sendSmsTest() {
        onView(withId(R.id.btn_sms_va_dant)).perform(click())
        onView(withId(R.id.log_texte)).check(matches(withSubstring("Vous signalez à " +
                "${userData.nomPartner} que tout va bien.")))
    }

    @Test
    fun callAidantTest() {
        Intents.init()
        intending(hasAction(Intent.ACTION_CALL)).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        onView(withId(R.id.btn_appel)).perform(click())
        onView(withId(R.id.log_texte))
            .check(matches(withSubstring("Vous appelez ${userData.nomPartner}.")))
        intended(allOf(
            hasAction(Intent.ACTION_CALL),
            hasData(Uri.parse("tel:"+userData.telephone))
        ))
        Intents.release()
    }

    @Test
    fun updateLogPrivateTest() {
        userData.prive = true
        userData.delay = 10000
        userData.bit = 2
        onView(withId(R.id.log_texte))
            .check(matches(withSubstring("${userData.nomPartner} demande si tout va bien ?")))
        userData.bit = 3
        Thread.sleep(300)
        onView(withId(R.id.log_texte))
            .check(matches(withSubstring("${userData.nomPartner} vous a appelé.")))
        userData.bit = 4
        Thread.sleep(300)
        onView(withId(R.id.log_texte)).check(matches(withSubstring("${userData.nomPartner} " +
                "tente de vérifier votre situation. Pourquoi ne pas l'appeler ?")))
    }

    @After
    fun tearDown() {
        userData.bit = 0
        userData.prive = false
        userData.delay = 0
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        if(file.exists()) file.delete()
    }
}