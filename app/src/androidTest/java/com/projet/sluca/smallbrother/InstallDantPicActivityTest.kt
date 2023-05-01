package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import android.content.Context
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
import androidx.test.rule.GrantPermissionRule
import com.projet.sluca.smallbrother.activities.InstallDantPicActivity
import com.projet.sluca.smallbrother.activities.QRCodeInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstallDantPicActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(InstallDantPicActivity::class.java)

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
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            assert(!userData.motion)
            userData.role = "Aidant"
            userData.nomPartner = "Jules"
        }
    }

    @Before
    fun setup() {
        Intents.init()
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