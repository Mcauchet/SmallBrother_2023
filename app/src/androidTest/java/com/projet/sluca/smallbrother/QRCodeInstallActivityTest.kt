package com.projet.sluca.smallbrother

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.projet.sluca.smallbrother.activities.QRCodeInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.SecurityUtils
import com.projet.sluca.smallbrother.utils.particule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class QRCodeInstallActivityTest {

    private lateinit var userData: UserData
    private lateinit var appContext: Context
    private lateinit var ivqrcode: ImageView

    @get:Rule
    val activityRule = ActivityScenarioRule(QRCodeInstallActivity::class.java)

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        userData = UserDataManager.getUserData(appContext.applicationContext as Application)

        activityRule.scenario.onActivity { activity ->
            SecurityUtils.getEncryptionKeyPair()
            SecurityUtils.getSignKeyPair()
            ivqrcode = activity.findViewById(R.id.ivqrcode)
            userData.nomPartner = "Julie"
        }
    }

    /*@Test
    fun testQRCodeDisplayed() {
        onView(withId(R.id.ivqrcode))
            .check(matches(isDisplayed()))
        var actualQRCodeBitmap: Bitmap? = null
        activityRule.scenario.onActivity {
            SecurityUtils.getEncryptionKeyPair()
            SecurityUtils.getSignKeyPair()
            actualQRCodeBitmap =
                (it.findViewById<ImageView>(R.id.ivqrcode).drawable as BitmapDrawable).bitmap
        }
        val expectedQRCodeBitmap = getEncodedQRCodeBitmap(SecurityUtils.getEncPublicKey())
        assertEquals(expectedQRCodeBitmap, actualQRCodeBitmap)
    }

    private fun getEncodedQRCodeBitmap(msg: String?): Bitmap {
        val qrEncoder = QRGEncoder(msg, null, QRGContents.Type.TEXT, 400)
        qrEncoder.colorBlack = Color.LTGRAY
        qrEncoder.colorWhite = Color.BLACK
        return qrEncoder.bitmap
    }*/

    @Test
    fun testEndButtonAidant() {
        userData.role = "Aidant"
        assert(userData.role == "Aidant")
        onView(withId(R.id.btn_terminer)).perform(click())
        onView(withId(R.id.textScan)).check(matches(isDisplayed()))
        userData.role = ""
    }

    @Test
    fun testEndButtonAide() {
        userData.role = "Aidé"
        assert(userData.role == "Aidé")
        onView(withId(R.id.btn_terminer)).perform(click())
        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))
        userData.role = ""
    }

    @After
    fun tearDown() {
    }
}