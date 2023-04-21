package com.projet.sluca.smallbrother

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.Launch1Activity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class Launch1ActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Launch1Activity::class.java)

    companion object {
        private lateinit var userData: UserData
        private lateinit var appContext: Context

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            appContext = InstrumentationRegistry.getInstrumentation().targetContext
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            assert(!userData.motion)
        }
    }

    @Before
    fun setup() {
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        if(file.exists())file.delete()
        file.createNewFile()
    }

    @Test
    fun checkFirstLaunchTestNoData() {
        onView(withId(R.id.btn_commencer))
            .check(matches(withText("Commencer")))
        onView(withId(R.id.btn_commencer)).perform(click())
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClassName("com.projet.sluca.smallbrother",
            "com.projet.sluca.smallbrother.activities.Launch2Activity")
        val result = InstrumentationRegistry.getInstrumentation().targetContext.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Assert.assertTrue(result.isNotEmpty())
        onView(withId(R.id.btn_role1)).check(matches(isDisplayed()))
    }

    @Test
    fun checkFirstLaunchTestData() {
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        val content =  "1\r" + "Aidant\r" + "3\r" + "4\r" + "5\r" + "6\r7"
        userData.writeDataInFile(file, content, appContext)

        val fileCreated = file.exists() && file.length() > 0
        assert(fileCreated)

        ActivityScenario.launch(Launch1Activity::class.java)

        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))

        file.delete()
        userData.role = null
    }

    @After
    fun tearDown() {
        val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
        if(file.exists()) file.delete()
    }
}