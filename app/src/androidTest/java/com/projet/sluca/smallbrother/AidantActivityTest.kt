package com.projet.sluca.smallbrother

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AidantActivityTest {

    private lateinit var scenario: ActivityScenario<AidantActivity>
    private lateinit var vibreur: Vibration
    private lateinit var userData: UserData

    @get:Rule
    val activityRule = ActivityScenarioRule(AidantActivity::class.java)

    @Before
    fun setUp() {
        scenario = activityRule.scenario
        vibreur = Vibration()
        val context = ApplicationProvider.getApplicationContext<Context>()
        userData = context.applicationContext as UserData
    }

    @Test
    fun testClickSettingsButton() {
        onView(withId(R.id.btn_reglages)).perform(click())
        onView(withId(R.id.btn_reinit_1)).check(matches(isDisplayed()))
    }

    @Test
    fun testClickPictureButton() {
        onView(withId(R.id.btn_photo)).perform(click())
        onView(withId(R.id.apercu)).check(matches(isDisplayed()))
    }

    @Test
    fun testClickReductButton() {
        onView(withId(R.id.btn_reduire)).perform(click())
        assertTrue(scenario.result.resultCode == Activity.RESULT_OK)
    }

    @Test
    fun testClickSmsAideButton() {
        onView(withId(R.id.btn_sms_va_dant)).perform(click())
        // To test SMS sending, you could use a mock SMS sending library such as SmsManager
        // and verify that it is called with the correct parameters.
    }

    @Test
    fun testClickCallButton() {
        //TODO
    }

    @Test
    fun testClickEmergencyButton() {
        //TODO
    }

    @Test
    fun testClickFilesButton() {
        onView(withId(R.id.btn_files)).perform(click())
        //TODO

        // To test the file download, you could use a mock HttpClient that returns a
        // response with the expected data and verify that the file is correctly saved.
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}