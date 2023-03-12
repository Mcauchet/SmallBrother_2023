package com.projet.sluca.smallbrother

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projet.sluca.smallbrother.activities.Launch1Activity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Launch1ActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Launch1Activity::class.java)

    @Test
    fun clickStartButton_launchesLaunch2Activity() {
        Espresso.onView(withId(R.id.btn_commencer)).perform(click())

        Espresso.onView(withId(R.id.btn_role1)).check(matches(isDisplayed()))
    }
}