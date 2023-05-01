package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import android.content.Context
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
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstallActivityTest {

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
            val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
            if(file.exists()) file.delete()
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            userData.role = "Aidant"
            assert(!userData.motion)
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
            if(file.exists()) file.delete()
        }
    }

    @Test
    fun checkAidantKeys() {
        userData.role = "Aidant"
        activityRule.scenario.onActivity {
            it.recreate()
        }
        assert(userData.role == "Aidant")
        SecurityUtils.getEncryptionKeyPair()
        onView(withId(R.id.input_nom)).check(matches(isDisplayed()))
        val pubKey = SecurityUtils.getEncPublicKey()
        assert(pubKey != "")
    }

    @Test
    fun registerValidDataAidant() {
        userData.role = "Aidant"
        activityRule.scenario.onActivity {
            it.recreate()
        }
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

        assert(userData.loadData(appContext))
        assertEquals(name, userData.nom)
        assertEquals(namePartner, userData.nomPartner)
        assertEquals(telephone, userData.telephone)
        onView(withId(R.id.btn_capture)).check(matches(isDisplayed()))
    }

    @Test
    fun registerValidDataAide() {
        userData.role = "Aidé"
        activityRule.scenario.onActivity {
            it.recreate()
        }
        val name = "Jane"
        val namePartner = "John"
        val telephone = "0475123456"

        val etName = onView(withId(R.id.input_nom))
        val etNamePartner = onView(withId(R.id.input_partner))
        val etTelephone = onView(withId(R.id.input_telephone))
        val btnContinue = onView(withId(R.id.btn_continue))

        etName.perform(typeText(name))
        etNamePartner.perform(typeText(namePartner))
        etTelephone.perform(typeText(telephone))
        btnContinue.perform(click())

        assert(userData.loadData(appContext))
        assertEquals(name, userData.nom)
        assertEquals(namePartner, userData.nomPartner)
        assertEquals(telephone, userData.telephone)
        onView(withId(R.id.textScan)).check(matches(isDisplayed()))
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

    @After
    fun tearDown() {
        val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
        if (file.exists()) file.delete()
    }
}