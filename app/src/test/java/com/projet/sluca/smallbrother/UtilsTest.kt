package com.projet.sluca.smallbrother

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
        println(time)
        Thread.sleep(1000)
        val time2 = getCurrentTime(format)
        println(time2)
        assertThat("First capture should be smaller than second one", time < time2)
    }
}