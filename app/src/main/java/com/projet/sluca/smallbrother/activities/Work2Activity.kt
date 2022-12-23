package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
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
 * @version 1.2 (Updated on 23-12-2022)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.
    private var batterie: String? = null // Retiendra le niveau de batterie restant.

    // Attribut de permission pour l'appel aux méthodes de "APictureCapturingService".
    // Must not be nullable in Kotlin in order for it to work
    private lateinit var pictureService: APictureCapturingService

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

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

        val location: String = getLocation()

        // --> [4] assemblage d'une archive ZIP.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12D)

        // Récupération des différents fichiers :

        val pathAudio = userData.path + "/SmallBrother/audio.ogg"
        val file1 = File(pathAudio)

        val pathPhoto1 = userData.getAutophotosPath(1)
        val file2 = File(pathPhoto1)

        val pathPhoto2 = userData.getAutophotosPath(2)
        val file3 = File(pathPhoto2)

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

        val informations = "Localisation $particule$nomAide : $location\n" +
                "Niveau de batterie : $batterie\n" +
                "En mouvement : $motion.\n"

        //add informations in a txt that is added to the zip archive
        val file4 = File(userData.path + "/SmallBrother/informations.txt")
        file4.createNewFile()
        val bufferedWriter = BufferedWriter(FileWriter(file4))

        bufferedWriter.write(informations)
        bufferedWriter.close()

        // Chemin de la future archive.
        val ziPath = this@Work2Activity.filesDir.path+"/SmallBrother/zippedFiles.zip"
        //Zip all files
        zipAll(
            this@Work2Activity.filesDir.path+"/SmallBrother",
            ziPath
        )

        object : Thread() {
            override fun run() {
                try {
                    val client = HttpClient(Android) {
                        install(ContentNegotiation) {
                            json()
                        }
                        install(HttpRequestRetry) {
                            retryOnServerErrors(maxRetries = 5)
                            exponentialDelay()
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var zipName = ""
                            if (File(ziPath).exists()) {
                                Log.d("zip file", "exists")
                                zipName = uploadZip(client, File(ziPath))
                            }
                            client.close()

                            val fileLoc =
                                "SmallBrother : $URLServer/download/$zipName [#SB10]"

                            sendSMS(this@Work2Activity, fileLoc, userData.telephone)

                            // Suppression des captures.
                            file1.delete()
                            file2.delete()
                            file3.delete()
                            file4.delete()

                            // Suppression du fichier ZIP.
                            val fileZ = File(ziPath)
                            fileZ.delete()

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

                vibreur.vibration(this@Work2Activity, 330) // vibration.

                // Réactivation du SmsReceiver.
                val pm = this@Work2Activity.packageManager
                val componentName = ComponentName(this@Work2Activity, SmsReceiver::class.java)
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                Log.d("emergencyIntent", intent.hasExtra("emergency").toString())
                if(intent.hasExtra("emergency")) {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${userData.telephone}")
                    }
                    startActivity(intent)
                    finish()
                }

                // Retour à l'écran de rôle de l'Aidé.
                val intent = Intent(this@Work2Activity, AideActivity::class.java)
                startActivity(intent)
            }
        }.start() // Envoi !
    }

    /***
     * function to get Location of Aide's phone
     *
     * @return a String with the address of the Aide or an error message if permissions not granted
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 22-12-2022)
     */
    private fun getLocation() : String {
        // Vérification obligatoire des permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return "Permission non accordée"//TODO check this (permission must not be granted)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        when {
            hasGps -> {
                val localGpsLocation = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                locationGps = localGpsLocation
            }
            hasNetwork -> {
                val localNetworkLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                locationNetwork = localNetworkLocation
            }
            else -> {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        return if(locationGps != null || locationNetwork != null) {
            getAddress()
        } else {
            "Permission non accordée."
        }
    }

    /***
     * function to get an address from a Location object
     */
    private fun getAddress() : String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        var adresses : List<Address>? = null
        if(locationGps != null) {
            adresses = geoCoder
                .getFromLocation(locationGps!!.latitude, locationGps!!.longitude, 1)
        } else if (locationNetwork != null) {
            adresses = geoCoder
                .getFromLocation(locationNetwork!!.latitude, locationNetwork!!.longitude, 1)
        }
        return adresses?.get(0)?.getAddressLine(0).toString()
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

        Log.d("before", "encryption")
        // chiffrement des données avec clé AES
        val aesKey = SecurityUtils.getAESKey()
        val encryptedData = SecurityUtils.encryptDataAes(file.readBytes(), aesKey)

        //chiffrement de la clé AES avec RSA+
        val aesEncKey = android.util.Base64
            .encodeToString(
                SecurityUtils.encryptAESKey(
                    loadPublicKey(userData.pubKey) as PublicKey,
                    aesKey
                ),
                android.util.Base64.NO_WRAP
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
            setBody(AideData(finalName, aesEncKey))
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