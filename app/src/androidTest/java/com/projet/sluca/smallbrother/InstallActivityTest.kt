package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.projet.sluca.smallbrother.activities.InstallActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstallActivityTest {

    private lateinit var userData: UserData

    @get:Rule
    val activityRule = ActivityScenarioRule(InstallActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS
    )

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        userData = UserDataManager.getUserData(appContext.applicationContext as Application)
        assert(!userData.motion)
    }

    @Test
    fun checkAidantKeys() {
        userData.role = "Aidant"
        assert(userData.role == "Aidant")
        SecurityUtils.getEncryptionKeyPair()
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
        val pubKey = SecurityUtils.getEncPublicKey()
        println(pubKey)
        assert(pubKey != "")
    }

    @Test
    fun registerValidData() {
        userData.role = "Aidant"
        val name = "John"
        val namePartner = "Jane"
        val telephone = "0475123456"

        val etName = onView(withId(R.id.input_nom))
        val etNamePartner = onView(withId(R.id.input_partner))
        val etTelephone = onView(withId(R.id.input_telephone))
        val btnContinue = onView(withId(R.id.btn_continue))

        etName.perform(typeText(name))
        etNamePartner.perform(typeText(namePartner))
        etTelephone.perform(typeText(telephone))
        btnContinue.perform(click())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userData = UserDataManager.getUserData(context.applicationContext as Application)
        assertEquals(name, userData.nom)
        assertEquals(namePartner, userData.nomPartner)
        assertEquals(telephone, userData.telephone)
        if(userData.role == "Aidant") {
            onView(withId(R.id.btn_capture)).check(matches(isDisplayed()))
        } else {
            onView(withId(R.id.textScan)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testInvalidTelephone() {
        val name = "John"
        val namePartner = "Jane"
        val telephone = "0675123456"

        val etName = onView(withId(R.id.input_nom))
        val etNamePartner = onView(withId(R.id.input_partner))
        val etTelephone = onView(withId(R.id.input_telephone))
        val btnContinue = onView(withId(R.id.btn_continue))

        etName.perform(typeText(name))
        etNamePartner.perform(typeText(namePartner))
        etTelephone.perform(typeText(telephone), closeSoftKeyboard())
        btnContinue.perform(click())

        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyFields() {
        val name = "John"
        val namePartner = ""
        val telephone = "0675123456"

        val etName = onView(withId(R.id.input_nom))
        val etNamePartner = onView(withId(R.id.input_partner))
        val etTelephone = onView(withId(R.id.input_telephone))
        val btnContinue = onView(withId(R.id.btn_continue))

        etName.perform(typeText(name))
        etNamePartner.perform(typeText(namePartner))
        etTelephone.perform(typeText(telephone), closeSoftKeyboard())
        btnContinue.perform(click())

        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
    }
}