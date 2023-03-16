package com.projet.sluca.smallbrother

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.InstallActivity
import com.projet.sluca.smallbrother.models.UserData
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstallActivityTest {

    private lateinit var scenario: ActivityScenario<InstallActivity>

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(InstallActivity::class.java)

    @Before
    fun setUp() {
        scenario = activityRule.scenario
    }

    @Test
    fun checkAideView() {
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
        onView(withId(R.id.input_partner)).check(matches(isDisplayed()))
        onView(withId(R.id.input_telephone)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_previous)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_continue)).check(matches(isDisplayed()))
    }

    @Test
    fun checkAidantView() {
        // Change the user role to aidant
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData
        userData.role = "Aidant"

        // Recreate the activity
        activityRule.scenario.recreate()

        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
        onView(withId(R.id.input_partner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.input_telephone)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_previous)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_continue)).check(matches(isDisplayed()))
    }

    @Test
    fun checkInputs() {
        // Change the user role to aidé
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData
        userData.role = "Aidé"

        onView(withId(R.id.input_nom)).perform(replaceText("John Doe"))
        onView(withId(R.id.input_partner)).perform(replaceText("Jane Doe"))
        onView(withId(R.id.input_telephone)).perform(replaceText("0486757473"))
        onView(withId(R.id.btn_continue)).perform(click())

        // Check that the next activity is started
        onView(withId(R.id.textScan)).check(matches(isDisplayed()))
    }

    @Test
    fun checkBadInputs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData
        userData.role = "Aidant"

        onView(withId(R.id.input_nom)).perform(replaceText("John Doe"))
        onView(withId(R.id.input_partner)).perform(replaceText("Jane Doe"))
        //Bad telephone format
        onView(withId(R.id.input_telephone)).perform(replaceText("1234567890"))
        onView(withId(R.id.btn_continue)).perform(click())

        //Check that we are still on the same activity
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}