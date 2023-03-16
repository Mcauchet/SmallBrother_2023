package com.projet.sluca.smallbrother

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.QRCodeScannerInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRCodeScannerInstallActivityTest {

    private lateinit var scenario: ActivityScenario<QRCodeScannerInstallActivity>

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(QRCodeScannerInstallActivity::class.java)

    @Before
    fun setUp() {
        scenario = activityScenarioRule.scenario
    }

    @Test
    fun testScanQRCode() {
        onView(withId(R.id.qr_scanner)).check(matches(isDisplayed()))

        val qrCode = "testQRCode"
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", qrCode)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.init()
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(result)

        onView(withId(R.id.qr_scanner)).perform(click())

        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData

        assertEquals(userData.pubKey, qrCode)
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}