package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.libs.*
import com.projet.sluca.smallbrother.models.AideData
import com.projet.sluca.smallbrother.models.UserData

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.security.PublicKey

import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/***
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 30-11-2022)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.
    private var urlGoogleMap: String? = null // Retiendra l'url vers la carte avec positionnement.
    private var batterie: String? = null // Retiendra le niveau de batterie restant.

    // Attribut de permission pour l'appel aux méthodes de "APictureCapturingService".
    // Must not be nullable in Kotlin in order for it to work
    private lateinit var pictureService: APictureCapturingService

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml (même écran).
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        // Liaison et remplissage des objets TextView.
        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
        loading(tvLoading) // Déclenchement de l'animation de chargement.

        // ================== [ Constitution du dossier joint ] ==================

        // --> [2] prise de deux photos automatiquement.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12B)

        // Lancement de la capture.
        Log.d("PIC SERVICE", "PIC SERVICE STARTS")
        pictureService = PictureCapturingServiceImpl.getInstance(this)
        pictureService.startCapturing(this, this)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Suite du processus après que les photos soient prises :
    override fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?) {
        Log.d("PIC SERVICE", "PIC SERVICE STOPS")
        // --> [3] localisation de l'Aidé.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12C)
        val latitude: String // Retiendra la coordonnée de latitiude.
        val longitude: String // Retiendra la coordonnée de longitude.

        // Vérification obligatoire des permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return //TODO check this (permission must not be granted)

        // Récupération des données de latitude et longitude de l'appareil.
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            // Mémorisation des coordonnées récupérées.
            latitude = location.latitude.toString() // latitude
            longitude = location.longitude.toString() // longitude

            // Construction de l'URL GoogleMap avec les coordonnées.
            urlGoogleMap = "http://maps.google.com/maps?q=$latitude,$longitude"
        }

        // --> [4] assemblage d'une archive ZIP.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12D)

        // Récupération des différents fichiers :

        val fichier1 = userData.path + "/SmallBrother/audio.ogg"
        val file1 = File(fichier1)

        val fichier2 = userData.getAutophotosPath(1)
        val file2 = File(fichier2)

        val fichier3 = userData.getAutophotosPath(2)
        val file3 = File(fichier3)

        // Chemin de la future archive.
        val ziPath = this@Work2Activity.filesDir.path+"/SmallBrother/zippedFiles.zip"

        zipAll(
            this@Work2Activity.filesDir.path+"/SmallBrother",
            ziPath
        )


        // --> [5] niveau de batterie.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12E)
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        batterie = "$level%"

        // --> [6] déterminer si en mouvement.
        val motion = if (userData.motion) "Oui" else "Non"

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12F)

        // Détermine la synthaxe du message selon la première lettre du nom de l'Aidé.
        val nomAide = userData.nom
        var particule = nomAide[0].toString()
        val voyelles = arrayOf(
            "A",
            "E",
            "Y",
            "U",
            "I",
            "O",
            "É",
            "È",
            "Œ",
            "a",
            "e",
            "y",
            "u",
            "i",
            "o",
            "é",
            "è"
        )

        particule = if (listOf(*voyelles).contains(particule)) " d'" else " de "

        val message = "Localisation $particule$nomAide : $urlGoogleMap\n" +
                "Niveau de batterie : $batterie\n" +
                "En mouvement : $motion.\n"

        //send localisation, battery and moving or not
        sendSMS(this@Work2Activity, message, userData.telephone)

        object : Thread() {
            override fun run() {
                try {
                    //TODO chiffrage des données
                    val client = HttpClient(Android) {
                        install(ContentNegotiation) {
                            json()
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var zipName = ""
                            if (File(ziPath).exists()) {
                                Log.d("zip file", "exists")
                                zipName = uploadZip(client, File(ziPath))
                            }
                            val fileLocation =
                                "SmallBrother : $URLServer/download/$zipName [#SB10]"

                            sendSMS(this@Work2Activity, fileLocation, userData.telephone)

                            // Suppression des captures.
                            file1.delete()
                            file2.delete()
                            file3.delete()

                            // Suppression du fichier ZIP.
                            val fileZ = File(ziPath)
                            fileZ.delete()

                            client.close()
                            Log.d("EOU", "upload ended")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (_: Exception) {
                }

                // Rafraîchissement du Log en fonction de la réussite du processus.
                CoroutineScope(Dispatchers.IO).launch {
                    if(isOnline(this@Work2Activity)) userData.refreshLog(11)
                    else userData.refreshLog(15)
                }

                // Concoction et envoi du SMS à l'Aidant.
                var sms = getString(R.string.smsys06)
                sms = sms.replace("§%", userData.nom)

                sendSMS(this@Work2Activity, sms, userData.telephone)

                vibreur.vibration(this@Work2Activity, 330) // vibration.

                // Réactivation du SmsReceiver.
                val pm = this@Work2Activity.packageManager
                val componentName = ComponentName(this@Work2Activity, SmsReceiver::class.java)
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                // Retour à l'écran de rôle de l'Aidé.
                val intent = Intent(this@Work2Activity, AideActivity::class.java)
                startActivity(intent)
            }
        }.start() // Envoi !
    }

    /***
     * renames the zip file and uploads it to the server
     *
     * @param [client] the HttpClient used to access the server
     * @param [file] the zip file to upload
     * @return the final name of the file to put in the download URL
     */
    suspend fun uploadZip(client: HttpClient, file: File): String {
        val newName = UUID.randomUUID().toString().substring(0..24)
        val finalName = "$newName.zip"

        //val encryptedData = encryptFileData(file.readBytes(), userData.pubKey)

        Log.d("before", "encryption")
        // chiffrement des données avec clé AES
        val encryptedData = SecurityUtils.encryptDataAes(file.readBytes())

        //chiffrement de la clé AES avec RSA+
        val aesEncKey = String(android.util.Base64.encode(
            SecurityUtils.encryptAESKey(
                loadPublicKey(userData.pubKey) as PublicKey), android.util.Base64.NO_WRAP)
        )
        Log.d("AES ENC KEY", aesEncKey)

        //envoi données chiffrées + clé AES chiffrée
        client.post("$URLServer/upload") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("description", "zipped files")
                    append("zip", encryptedData, Headers.build {
                        append(HttpHeaders.ContentType, "application/zip")
                        append(HttpHeaders.ContentDisposition, "filename=\"$finalName\"")
                    })
                },
                boundary = "WebAppBoundary"
            )
            )
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }
        client.post("$URLServer/upload/aes") {
            contentType(ContentType.Application.Json)
            setBody(AideData("$URLServer/upload/$finalName", aesEncKey))
        }
        return finalName
    }

    /***
     * code snippet from https://www.folkstalk.com/tech/how-to-zip-folders-subfolders-with-files-in-it-in-kotlin-using-zipoutputstream-with-code-solution/
     */
    private fun zipAll(dir: String, zipFile: String) {
        val sourceFile = File(dir)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOutputStream ->
            zipOutputStream.use { stream ->
                zipFiles(stream, sourceFile, "")
            }
        }
    }

    /***
     * code snippet from https://www.folkstalk.com/tech/how-to-zip-folders-subfolders-with-files-in-it-in-kotlin-using-zipoutputstream-with-code-solution/
     */
    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val data = ByteArray(2048)
        for (file in sourceFile.listFiles()!!) {
            if(file.isDirectory) {
                val entry = ZipEntry(file.name+File.separator)
                entry.time = file.lastModified()
                entry.isDirectory
                entry.size = file.length()
                Log.i("zip", "Adding Directory: " + file.name)
                zipOut.putNextEntry(entry)
                //Call recursively to add files within this directory
                zipFiles(zipOut, file, file.name)
            } else {
                if (!file.name.contains(".zip")) { //If folder contains a file with extension ".zip", skip it
                    FileInputStream(file).use { fileI ->
                        BufferedInputStream(fileI).use { origin ->
                            val path = parentDirPath + File.separator + file.name
                            Log.i("zip", "Adding file: $path")
                            val entry = ZipEntry(path)
                            entry.time = file.lastModified()
                            entry.isDirectory
                            entry.size = file.length()
                            zipOut.putNextEntry(entry)
                            while (true) {
                                val readBytes = origin.read(data)
                                if (readBytes == -1) {
                                    break
                                }
                                zipOut.write(data, 0, readBytes)
                            }
                        }
                    }
                } else {
                    zipOut.closeEntry()
                    zipOut.close()
                }
            }
        }
    }

    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {}

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}