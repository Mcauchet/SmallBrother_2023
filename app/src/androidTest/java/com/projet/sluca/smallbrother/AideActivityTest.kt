package com.projet.sluca.smallbrother

import android.os.Handler
import android.service.autofill.UserData
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.projet.sluca.smallbrother.activities.AideActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AideActivityTest {

    private lateinit var activity: AideActivity

    @Mock
    private lateinit var userData: UserData
    @Mock
    private lateinit var tvLog: TextView
    @Mock
    private lateinit var tvDelay: TextView
    @Mock
    private lateinit var tvIntituleDelay: TextView
    @Mock
    private lateinit var btnPrivate: Switch
    @Mock
    private lateinit var ivLogo: ImageView
    @Mock
    private lateinit var logHandler: Handler
    @Mock
    private lateinit var vibreur: Vibration

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        activity = Robolectric.buildActivity(AideActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
    }

    @Test
    fun testOnCreate() {
        assertNotNull(tvLog)
        assertNotNull(tvDelay)
        assertNotNull(tvIntituleDelay)
        assertNotNull(btnPrivate)
        assertNotNull(ivLogo)
        assertNotNull(logHandler)
        assertNotNull(activity.userData)

        activity.btnPrivate.isChecked = true
        assertTrue(btnPrivate.isChecked)

        /*activity.btnPrivate.onCheckedChanged(btnPrivate, true)
        verify(activity.vibreur, times(1)).vibration(activity, 100)

        activity.btnPrivate.onCheckedChanged(activity.btnPrivate, false)
        verify(activity.vibreur, times(1)).vibration(activity, 200)*/

        activity.updateAideInfo()
        verify(activity.vibreur, times(1)).vibration(activity, 330)

        verifyNoMoreInteractions(userData, tvLog, tvDelay, tvIntituleDelay, btnPrivate, ivLogo, vibreur)
    }
}