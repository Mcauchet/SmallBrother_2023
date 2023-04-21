package com.projet.sluca.smallbrother

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class AideActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AideActivity::class.java)

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
            userData.role = "Aidé"
            userData.nom = "Jules"
            userData.telephone = "0476546545"
            userData.pubKey = "FakePublicKey"
            userData.nomPartner = "Émilie"
            userData.path = appContext.filesDir.path
            userData.prive = false
            userData.bit = 0
            userData.saveData(appContext)
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
            if(file.exists()) file.delete()
        }
    }

    @Before
    fun setUp() {
        Intents.init()
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
        println(userData.delay)
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

    @Test
    fun buttonReductTest() {
        onView(withId(R.id.btn_reduire)).perform(click())
        Thread.sleep(500)
        val lifeCycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
        lateinit var resumedActivities: Collection<Activity>
        lateinit var pausedActivities: Collection<Activity>
        activityRule.scenario.onActivity {
            resumedActivities = lifeCycleMonitor.getActivitiesInStage(Stage.RESUMED)
            pausedActivities = lifeCycleMonitor.getActivitiesInStage(Stage.PAUSED)
        }
        assert(resumedActivities.isEmpty())
        assert(pausedActivities.isNotEmpty())
    }

    @Test
    fun sendSmsTest() {
        onView(withId(R.id.btn_sms_va_dant)).perform(click())
        Thread.sleep(300)
        onView(withId(R.id.log_texte)).check(matches(withSubstring("Vous signalez à " +
                "${userData.nomPartner} que tout va bien.")))
    }

    @Test
    fun callAidantTest() {
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
    }

    @Test
    fun updateLogPrivateTest() {
        userData.prive = true
        userData.delay = 10000
        userData.bit = 2
        Thread.sleep(300)
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
        activityRule.scenario.onActivity { activity ->
            if(activity.btnPrivate.isChecked) activity.btnPrivate.toggle()
        }
        Intents.release()
    }
}
