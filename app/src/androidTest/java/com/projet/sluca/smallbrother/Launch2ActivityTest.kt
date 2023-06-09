package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.projet.sluca.smallbrother.activities.Launch2Activity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class Launch2ActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Launch2Activity::class.java)

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

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            assert(!userData.motion)
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            userData.role = null
        }
    }

    @Test
    fun checkRoleAidant() {
        onView(withId(R.id.btn_role1)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_role1)).perform(click())
        assert(userData.role == "Aidant")
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
    }

    @Test
    fun checkRoleAide() {
        onView(withId(R.id.btn_role2)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_role2)).perform(click())
        assert(userData.role == "Aidé")
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
    }

    @Test
    fun backButtonInstallActivityTest() {
        onView(withId(R.id.btn_role1)).perform(click())
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_previous)).perform(click())
        onView(withId(R.id.btn_role1)).check(matches(isDisplayed()))
    }
}