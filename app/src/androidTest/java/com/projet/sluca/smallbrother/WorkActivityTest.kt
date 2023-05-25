package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class WorkActivityTest {

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
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
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
            SecurityUtils.getEncryptionKeyPair()
            SecurityUtils.getSignKeyPair()
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            userData.version = "1.2"
            userData.role = "Aidé"
            userData.nom = "Jules"
            userData.telephone = "0476546545"
            userData.pubKey = SecurityUtils.getEncPublicKey()
            userData.nomPartner = "Émilie"
            userData.path = appContext.filesDir.path
            userData.prive = false
            userData.bit = 0
            userData.saveData(appContext)

        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            val file = File(appContext.filesDir, "SmallBrother/donnees.txt")
            if(file.exists()) file.delete()
        }
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun contextCaptureTest() {
        launch(AideActivity::class.java)
        val intent = Intent(appContext, WorkActivity::class.java)
        intent.putExtra("clef", "[#SB04]")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
        Thread.sleep(30000)
        CoroutineScope(Dispatchers.IO).launch {
            withTimeout(30000) {
                val secretKey = SecurityUtils.getAESKey()
                val secretKey2 = SecurityUtils.getAESKey()
                assert(secretKey != secretKey2)
                onView(withId(R.id.btn_urgence)).check(matches(isDisplayed()))
            }
        }
    }
}