package com.projet.sluca.smallbrother

import android.app.Application
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.InstallDantPicActivity
import com.projet.sluca.smallbrother.activities.QRCodeInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstallDantPicActivityTest {

    lateinit var userData: UserData

    @get:Rule
    val activityRule = ActivityScenarioRule(InstallDantPicActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        userData = UserDataManager.getUserData(appContext.applicationContext as Application)
        assert(!userData.motion)
        userData.role = "Aidant"
        userData.nomPartner = "Jules"
    }

    @Test
    fun testCaptureButton() {
        onView(withId(R.id.btn_capture)).perform(click())
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    @Test
    fun testBackButton() {
        onView(withId(R.id.btn_previous)).perform(click())
        activityRule.scenario.onActivity { activity ->
            assert(activity.isFinishing)
        }
    }

    @Test
    fun testContinueButton() {
        onView(withId(R.id.btn_continue)).perform(click())
        intended(hasComponent(QRCodeInstallActivity::class.java.name))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}