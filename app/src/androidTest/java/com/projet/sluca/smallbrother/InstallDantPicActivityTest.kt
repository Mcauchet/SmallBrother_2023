package com.projet.sluca.smallbrother

import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.InstallDantPicActivity
import com.projet.sluca.smallbrother.activities.QRCodeInstallActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstallDantPicActivityTest {

    private lateinit var scenario: ActivityScenario<InstallDantPicActivity>

    @get:Rule
    val activityRule = ActivityScenarioRule(InstallDantPicActivity::class.java)

    @Before
    fun setUp() {
        scenario = activityRule.scenario
    }

    @Test
    fun captureButton_click_opensCamera() {
        //Find the capture button and click it
        onView(withId(R.id.btn_capture)).perform(click())
        //Check that the camera is opened
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    @Test
    fun backButton_click_finishesActivity() {
        //Find the back button and click it
        onView(withId(R.id.btn_previous)).perform(click())
        //Check that the activity is finished
        scenario.onActivity { activity ->
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun testEndButton() {
        onView(withId(R.id.btn_terminer)).perform(click())
        intended(hasComponent(QRCodeInstallActivity::class.java.name))
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}