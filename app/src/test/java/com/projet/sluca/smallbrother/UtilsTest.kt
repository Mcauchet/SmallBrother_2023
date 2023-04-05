package com.projet.sluca.smallbrother

import com.projet.sluca.smallbrother.utils.getCurrentTime
import com.projet.sluca.smallbrother.utils.particule
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UtilsTest {
    @Test fun particuleOK_de() {
        val name = "Marie"
        val particule = particule(name)
        assertThat("Particule for $name should be 'de ' ", particule == "de ")
    }

    @Test fun particuleOK_d() {
        val name = "Elsa"
        val particule = particule(name)
        assertThat("Particule for $name should be 'd''", particule == "d'")
    }

    @Test fun getTwoDifferentCurrentTime_OK() {
        val format = "dd-MM-yyyy hh:mm:ss"
        val time = getCurrentTime(format)
        Thread.sleep(1000)
        val time2 = getCurrentTime(format)
        assertThat("First capture should be smaller than second one", time < time2)
    }
}