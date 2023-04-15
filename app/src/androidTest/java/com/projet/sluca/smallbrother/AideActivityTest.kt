package com.projet.sluca.smallbrother

import android.app.Application
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.getCurrentTime
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
        onView(withText("${getCurrentTime("HH:mm")}: Vous avez activé le mode privé.")).check(matches(isDisplayed()))
        //uncheck switch
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isNotChecked()))
        assert(!userData.prive)
        assert(userData.bit == 0)
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
        //uncheck switch
        onView(withId(R.id.btn_deranger)).perform(click())
        assert(!userData.prive)
        assert(userData.bit == 0)
    }

    @Test
    fun privateModeCancelTest() {
        onView(withId(R.id.btn_deranger)).perform(click())
        onView(withText("Annuler")).perform(click())
        onView(withId(R.id.btn_deranger)).check(matches(isNotChecked()))
        assert(!userData.prive)
        assert(userData.bit == 0)
    }

    @After
    fun tearDown() {
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        if(file.exists()) file.delete()
    }
}