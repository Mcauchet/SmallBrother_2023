package com.projet.sluca.smallbrother

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.activities.InstallDantPicActivity
import com.projet.sluca.smallbrother.activities.QRCodeInstallActivity
import com.projet.sluca.smallbrother.activities.QRCodeScannerInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRCodeInstallActivityTest {

    private lateinit var scenario: ActivityScenario<QRCodeInstallActivity>

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(QRCodeInstallActivity::class.java)

    @Before
    fun setUp() {
        scenario = activityScenarioRule.scenario
    }

    @Test
    fun testQRCodeInstallActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData

        scenario.onActivity { activity ->
            assertNotNull(activity)
        }

        onView(withId(R.id.textQR)).check(matches(withText(containsString("Scannez"))))

        onView(withId(R.id.btn_terminer)).perform(click())

        if(userData.role == "Aidant") {
            intended(hasComponent(QRCodeScannerInstallActivity::class.java.name))
        } else if (userData.role == "Aidé") {
            intended(hasComponent(AideActivity::class.java.name))
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}