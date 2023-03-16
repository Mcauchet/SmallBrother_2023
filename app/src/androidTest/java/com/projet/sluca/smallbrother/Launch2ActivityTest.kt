package com.projet.sluca.smallbrother

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.Launch2Activity
import com.projet.sluca.smallbrother.models.UserData
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Launch2ActivityTest {

    private lateinit var scenario: ActivityScenario<Launch2Activity>

    @get:Rule
    val activityRule = ActivityScenarioRule(Launch2Activity::class.java)

    @Before
    fun setUp() {
        scenario = activityRule.scenario
    }

    @Test
    fun testRoleSelectionButtons() {
        val btnRoleAidant = Espresso.onView(withId(R.id.btn_role1))
        val btnRoleAide = Espresso.onView(withId(R.id.btn_role2))

        btnRoleAidant.perform(click())

        /*val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
                    as Singleton*/
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userData = context.applicationContext as UserData
        assertEquals("Aidant", userData.role)

        btnRoleAide.perform(click())

        assertEquals("Aidé", userData.role)
    }

    @After
    fun tearDown() {
        scenario.close()
    }
}