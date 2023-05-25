package com.projet.sluca.smallbrother.utils

/**
 * Gets the light sensor interpretation for the information file
 * @param level the results of the sensor
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 24-05-2023)
 */
fun getLightScale(level: Float) = when(level) {
    in 0f..50f -> "Très faible - ${level.toInt()}"
    in 51f..200f -> "Faible - ${level.toInt()}"
    in 201f..1000f -> "Normal - ${level.toInt()}"
    in 1001f..10000f -> "Clair - ${level.toInt()}"
    else -> "Très clair - ${level.toInt()}"
}

/**
 * Interpret the results of the motion capture based on the start and end of the audio record
 * @param checkAcc1 true if acceleration above threshold at second 2 of the record,
 * false otherwise
 * @param checkAcc2 true if acceleration above threshold at second 9 of the record,
 * false otherwise
 * @return the interpretation as a String
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 11-04-2023)
 */
fun interpretAcceleration(checkAcc1: Boolean, checkAcc2: Boolean) = when {
    checkAcc1 && checkAcc2 -> "En mouvement"
    checkAcc1 && !checkAcc2 -> "S'est arrêté"
    !checkAcc1 && checkAcc2 -> "Commence à bouger"
    else -> "À l'arrêt"
}

/**
 * Interpret the motion data to determine the movement state of the phone
 * @param acc the acceleration interpretation of the phone
 * @param xyz true if x, y and z are the same at the start and end of the audio recording,
 * false otherwise
 * @param addressDiff true if Locations are different with an interval of 10 seconds,
 * false otherwise
 * @return the interpretation as a String
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 06-05-2023)
 */
fun interpretMotionData(acc: String, xyz: Boolean, addressDiff: Boolean) = when {
    (acc == "En mouvement" || acc == "Commence à bouger") && !xyz && addressDiff ->
        "En mouvement, probablement à pied."
    (acc == "En mouvement" || acc == "Commence à bouger") && !xyz && !addressDiff ->
        "Se déplace mais reste au même endroit (magasin, maison, etc.)."
    (acc == "À l'arrêt" || acc == "S'est arrêté") && xyz && !addressDiff -> "À l'arrêt."
    (acc == "À l'arrêt" || acc == "S'est arrêté") && !xyz && addressDiff ->
        "Se déplace, probablement dans un véhicule."
    (acc == "À l'arrêt" || acc == "S'est arrêté") && xyz && addressDiff ->
        "Semble à l'arrêt, le GPS peut être imprécis."
    (acc == "À l'arrêt" || acc == "S'est arrêté") && !xyz && !addressDiff ->
        "Se déplace très lentement ou à l'arrêt."
    else -> "Déplacement indéterminé."
}