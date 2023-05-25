package com.projet.sluca.smallbrother

import com.projet.sluca.smallbrother.utils.getLightScale
import com.projet.sluca.smallbrother.utils.interpretAcceleration
import com.projet.sluca.smallbrother.utils.interpretMotionData
import org.junit.Test

class WorkUtilsTest {
    @Test
    fun getLightScaleTest() {
        assert(getLightScale(0f) == "Très faible - 0")
        assert(getLightScale(50f) == "Très faible - 50")
        assert(getLightScale(51f) == "Faible - 51")
        assert(getLightScale(200f) == "Faible - 200")
        assert(getLightScale(201f) == "Normal - 201")
        assert(getLightScale(1000f) == "Normal - 1000")
        assert(getLightScale(1001f) == "Clair - 1001")
        assert(getLightScale(10000f) == "Clair - 10000")
        assert(getLightScale(10001f) == "Très clair - 10001")
        assert(getLightScale(1000000f) == "Très clair - 1000000")
    }

    @Test
    fun interpretAccelerationTest() {
        assert(interpretAcceleration(checkAcc1 = true, checkAcc2 = true) == "En mouvement")
        assert(interpretAcceleration(checkAcc1 = true, checkAcc2 = false) == "S'est arrêté")
        assert(interpretAcceleration(checkAcc1 = false, checkAcc2 = true) == "Commence à bouger")
        assert(interpretAcceleration(checkAcc1 = false, checkAcc2 = false) == "À l'arrêt")
    }

    @Test
    fun interpretMotionDataTest() {
        assert(interpretMotionData("En mouvement", xyz = false, addressDiff = true)
                == "En mouvement, probablement à pied.")
        assert(interpretMotionData("Commence à bouger", xyz = false, addressDiff = true)
                == "En mouvement, probablement à pied.")
        assert(interpretMotionData("En mouvement", xyz = false, addressDiff = false)
                == "Se déplace mais reste au même endroit (magasin, maison, etc.).")
        assert(interpretMotionData("Commence à bouger", xyz = false, addressDiff = false)
                == "Se déplace mais reste au même endroit (magasin, maison, etc.).")
        assert(interpretMotionData("À l'arrêt", xyz = true, addressDiff = false)
                == "À l'arrêt.")
        assert(interpretMotionData("S'est arrêté", xyz = true, addressDiff = false)
                == "À l'arrêt.")
        assert(interpretMotionData("À l'arrêt", xyz = false, addressDiff = true)
                == "Se déplace, probablement dans un véhicule.")
        assert(interpretMotionData("S'est arrêté", xyz = false, addressDiff = true)
                == "Se déplace, probablement dans un véhicule.")
        assert(interpretMotionData("À l'arrêt", xyz = true, addressDiff = true)
                == "Semble à l'arrêt, le GPS peut être imprécis.")
        assert(interpretMotionData("S'est arrêté", xyz = true, addressDiff = true)
                == "Semble à l'arrêt, le GPS peut être imprécis.")
        assert(interpretMotionData("À l'arrêt", xyz = false, addressDiff = false)
                == "Se déplace très lentement ou à l'arrêt.")
        assert(interpretMotionData("S'est arrêté", xyz = false, addressDiff = false)
                == "Se déplace très lentement ou à l'arrêt.")
        assert(interpretMotionData("En mouvement", xyz = true, addressDiff = true)
                == "Déplacement indéterminé.")
        assert(interpretMotionData("Commence à bouger", xyz = true, addressDiff = true)
                == "Déplacement indéterminé.")
    }
}