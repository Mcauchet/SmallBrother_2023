package com.projet.sluca.smallbrother

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.google.android.gms.vision.barcode.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.projet.sluca.smallbrother.activities.QRCodeScannerInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
class QRCodeScannerInstallActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(QRCodeScannerInstallActivity::class.java)

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
        private lateinit var codeScanner: CodeScanner
        private lateinit var scannerView: CodeScannerView

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            appContext = InstrumentationRegistry.getInstrumentation().targetContext
            userData = UserDataManager.getUserData(appContext.applicationContext as Application)
            userData.nomPartner = "Julie"
            userData.role = "Aidant"
            userData.nom = "Marc"
            userData.version = "1.2"
            userData.telephone = "0476181818"
            userData.path = "${appContext.applicationContext.filesDir}/SmallBrother/"
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            val file = File(appContext.filesDir, "/SmallBrother/donnees.txt")
            if (file.exists()) file.delete()
        }
    }

    @Before
    fun setup() {
        activityRule.scenario.onActivity {
            scannerView = CodeScannerView(appContext)
            codeScanner = CodeScanner(appContext, scannerView)
        }
    }

    @Test
    fun textScanTest() {
        val textScan = onView(withId(R.id.textScan))
        textScan.check(matches(isDisplayed()))
        activityRule.scenario.onActivity {
            CoroutineScope(Dispatchers.IO).launch{
                textScan.check(matches(withText("[INSTALLATION] Veuillez scanner le QR code sur " +
                        "le smartphone de Julie.")))
            }
        }
    }

    @Test
    fun scanQRTest() {
        val activityScenario = launch(QRCodeScannerInstallActivity::class.java)
        activityScenario.onActivity { activity ->
            val codeScannerField = QRCodeScannerInstallActivity::class.java.getDeclaredField("codeScanner")
            codeScannerField.isAccessible = true
            val codeScanner = codeScannerField.get(activity) as CodeScanner
            val pubKey = "FakePublicKey"
            val barcode = Barcode().apply { rawValue = pubKey }
            val result = Result(barcode.rawValue, null, null, BarcodeFormat.QR_CODE)
            codeScanner.decodeCallback?.onDecoded(result)
        }
        onView(withId(R.id.btn_sms_va_dant)).check(matches(isDisplayed()))
    }
}