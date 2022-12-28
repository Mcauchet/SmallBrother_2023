package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.R
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
import kotlinx.coroutines.*
import java.io.*
import java.security.PublicKey

import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/***
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 28-12-2022)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.
    private var batterie: String? = null // Retiendra le niveau de batterie restant.

    // Must not be nullable in Kotlin in order for it to work
    private lateinit var pictureService: APictureCapturingService

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
        loading(tvLoading) // Déclenchement de l'animation de chargement.

        getLocation()

        // ================== [ Constitution du fichier zip ] ==================

        // --> [2] prise de deux photos automatiquement.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12B)

        // Lancement de la capture.
        pictureService = PictureCapturingServiceImpl.getInstance(this)
        pictureService.startCapturing(this, this)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Suite du processus après que les photos soient prises :
    override fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?) {
        // --> [3] localisation de l'Aidé.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12C)

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

        // --> [7] Light level
        val light = if(intent.hasExtra("light")) intent.getFloatExtra("light", -1f) else -1f

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12F)

        // Détermine la syntaxe du message selon la première lettre du nom de l'Aidé.
        val nomAide = userData.nom

        val particule = particule(nomAide)

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
                    val location: String = if(locationGps != null || locationNetwork != null){
                        getAddress()
                    } else {
                        "Erreur lors de la récupération de la position"
                    }

                    val informations = "Localisation $particule$nomAide : $location\n" +
                            "Niveau de batterie : $batterie\n" +
                            "En mouvement : $motion.\n" +
                            "Niveau de lumière (en lux) : $light.\n" // TODO Explicit interpretation needed

                    Log.d("infos", informations)

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
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var zipName = ""
                            if (File(ziPath).exists()) {
                                Log.d("zip file", "exists")
                                zipName = uploadZip(client, File(ziPath))
                            }
                            client.close()

                            val fileLocMsg = getString(R.string.smsys10)
                                .replace("§%", "$URLServer/download/$zipName")

                            //sendSMS(this@Work2Activity, fileLocMsg, userData.telephone)

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
     * @version 1.2 (Updated on 28-12-2022)
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Vérification obligatoire des permissions.
        requestPermission()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps || hasNetwork) {
            fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location = task.result
                if(location != null) {
                    locationGps = location
                    locationNetwork = location
                } else {
                    requestNewLocationData()
                }
            }
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     * request new location data if no previous one existing
     *
     * @author Maxime Caucheteur (from https://www.androidhire.com/current-location-in-android-using-kotlin/)
     * @version 1.2 (Updated on 28-12-22)
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        requestPermission()
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationGps = locationResult.lastLocation
            locationNetwork = locationResult.lastLocation
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        } else {
            Toast.makeText(this, "Location permission was denied", Toast.LENGTH_SHORT).show()
        }
        return
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

        // chiffrement des données avec clé AES
        val aesKey = SecurityUtils.getAESKey()
        val encryptedData = SecurityUtils.encryptDataAes(file.readBytes(), aesKey)

        //chiffrement de la clé AES avec RSA+
        val aesEncKey = android.util.Base64
            .encodeToString(
                SecurityUtils.encryptAESKey(
                    SecurityUtils.loadPublicKey(userData.pubKey) as PublicKey,
                    aesKey
                ),
                android.util.Base64.NO_WRAP
            )

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
     * zips all file in a given dir
     * @param [dir] the directory where the files are
     * @param [zipFile] the name of the final zip file
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
     * Zips a file
     * @param [zipOut] the zip output stream
     * @param [sourceFile] the File to zip
     * @param [parentDirPath] the path of the sourceFile's parent
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
                //If folder contains a file with extension ".zip", skip it
                if (!file.name.contains(".zip")) {
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