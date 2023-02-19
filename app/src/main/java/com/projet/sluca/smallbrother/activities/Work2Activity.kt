package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.annotation.SuppressLint
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
import android.telephony.TelephonyManager
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
import javax.crypto.SecretKey

/***
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 19-02-2023)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView
    private var battery: String? = null

    // Must not be nullable in Kotlin in order for it to work
    private lateinit var pictureService: APictureCapturingService

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var zipName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        userData = application as UserData
        loading(tvLoading)
        setAppBarTitle(userData, this)

        // --> [2] Get Aide Location
        tvAction.text = getString(R.string.message12C)
        checkForLocation()

        // --> [3] Capture of front and back pictures
        tvAction.text = getString(R.string.message12B)
        pictureService = PictureCapturingServiceImpl.getInstance(this)
        pictureService.startCapturing(this, this)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?) {
        // --> [4] Get all needed files reference.
        tvAction.text = getString(R.string.message12D)

        val pathAudio = userData.path + "/SmallBrother/audio.ogg"
        val audioFile = File(pathAudio)
        val pathPhoto1 = userData.getAutophotosPath(1)
        val firstPicture = File(pathPhoto1)
        val pathPhoto2 = userData.getAutophotosPath(2)
        val secondPicture = File(pathPhoto2)

        // --> [5] Battery level.
        tvAction.text = getString(R.string.message12E)
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        battery = "$level%"

        // --> [6] Fetch motion data.
        val motion = if(intent.hasExtra("interpretation"))
            intent.getStringExtra("interpretation").toString() else "Indéterminé"

        // --> [7] Get light level
        val light = if(intent.hasExtra("light")) intent.getFloatExtra("light", -1f) else -1f

        tvAction.text = getString(R.string.message12F)

        // Checks what particule should be used with the partner name
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

                    val currentTime = getCurrentTime("dd/MM/yyyy HH:mm:ss")

                    val information = "Localisation $particule$nomAide : $location\n" +
                            "Niveau de batterie : $battery\n" +
                            "En mouvement : $motion.\n" +
                            "Niveau de lumiere (en lux) : $light.\n" + // TODO Explicit interpretation needed
                            "Date de la capture : $currentTime\n"

                    Log.d("infos", information)

                    val informationFile = File(userData.path + "/SmallBrother/informations.txt")
                    informationFile.createNewFile()

                    val bufferedWriter = BufferedWriter(FileWriter(informationFile))
                    bufferedWriter.write(information)
                    bufferedWriter.close()

                    val ziPath = this@Work2Activity.filesDir.path+"/SmallBrother/zippedFiles.zip"

                    zipAll(this@Work2Activity.filesDir.path+"/SmallBrother", ziPath)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (File(ziPath).exists()) {
                                zipName = uploadZip(client, File(ziPath))
                                assert(zipName != "")
                            }
                            client.close()

                            val fileLocMsg = getString(R.string.smsys10)
                                .replace("§%", "$URLServer/download/$zipName")

                            sendSMS(this@Work2Activity, fileLocMsg, userData.telephone, vibreur)

                            audioFile.delete()
                            firstPicture.delete()
                            secondPicture.delete()
                            informationFile.delete()
                            val zipFile = File(ziPath)
                            zipFile.delete()
                            deleteLocation()
                            Log.i("EOU", "upload ended")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (_: Exception) {
                }

                vibreur.vibration(this@Work2Activity, 330)

                activateSMSReceiver(this@Work2Activity)

                Log.d("emergencyIntent", intent.hasExtra("emergency").toString())
                if(intent.hasExtra("emergency")) {
                    /*val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${userData.telephone}")
                    }
                    startActivity(intent)*/
                    finish()
                }
                val intent = Intent(this@Work2Activity, AideActivity::class.java)
                startActivity(intent)
            }
        }.start()
    }

    /***
     * function to get Location of Aide's phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    @SuppressLint("MissingPermission")
    private fun checkForLocation() {
        requestPermission()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        /*if(!hasGps && !hasNetwork) {
            getLocation()
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }*/
        if(!hasGps && !hasNetwork) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        assert(hasGps || hasNetwork)
        getLocation() //TODO Test this
    }

    /**
     * Retrieve location as a Location object inside locationGps and locationNetwork
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location = task.result
            if(location != null) {
                locationGps = location
                locationNetwork = location
            } else {
                requestNewLocationData()
            }
        }
    }

    private fun deleteLocation() {
        locationGps = null
        locationNetwork = null
    }

    /***
     * function to get an address from a Location object
     * @return the address as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
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

    /**
     * request new location data if no previous one existing
     *
     * @author Maxime Caucheteur (from https://www.androidhire.com/current-location-in-android-using-kotlin/)
     * @version 1.2 (Updated on 28-12-22)
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        requestPermission()
        val locationRequest = LocationRequest() //Check for this deprecated, doesn't look too hard
        locationRequest.priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
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

    /***
     * renames the zip file and uploads it to the server
     *
     * @param [client] the HttpClient used to access the server
     * @param [file] the zip file to upload
     * @return the final name of the file to put in the download URL
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    suspend fun uploadZip(client: HttpClient, file: File): String {
        require(file.exists())
        val finalName = generateRandomName()
        val aesKey = SecurityUtils.getAESKey()
        val encryptedData = SecurityUtils.encryptDataAes(file.readBytes(), aesKey)
        val signature = SecurityUtils.signFile(encryptedData)
        val signString = android.util.Base64.encodeToString(signature, android.util.Base64.NO_WRAP)
        val aesEncKey = encryptAESKeyWithRSA(aesKey)
        uploadFileRequest(client, encryptedData, finalName)
        client.post("$URLServer/upload/aes") {
            contentType(ContentType.Application.Json)
            setBody(AideData(finalName, aesEncKey, signString))
        }
        return finalName
    }

    /**
     * Generates a random name with randomUUID generator of a size of nameSize
     * @return the new name of the file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun generateRandomName(): String {
        val nameSize = 25
        val newName = UUID.randomUUID().toString().substring(0 until nameSize)
        return "$newName.zip"
    }

    /**
     * Encrypt the AES key (that encrypts the data) with the RSA public key
     * @param [aesKey] The AES key retrieved in the Android Keystore
     * @return the encrypted AES key as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun encryptAESKeyWithRSA(aesKey: SecretKey) : String {
        check(userData.pubKey != "")
        return android.util.Base64.encodeToString(
                SecurityUtils.encryptAESKey(
                    SecurityUtils.loadPublicKey(userData.pubKey) as PublicKey,
                    aesKey
                ),
                android.util.Base64.NO_WRAP
            )
    }

    /**
     * Request to upload the zip file to the server
     * @param [client] the HttpClient to access Ktor server
     * @param [encryptedData] the encrypted data of the zip file
     * @param [finalName] the final name of the file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private suspend fun uploadFileRequest(client: HttpClient, encryptedData: ByteArray,
                                          finalName: String) {
        client.post("$URLServer/upload") {
            configureUploadRequestBody(this, encryptedData, finalName) // test this
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }
    }

    /**
     * Configure the Body of the HttpRequest
     * @param [httpRB] the RequestBuilder
     * @param [encryptedData] the encrypted data of the zip file
     * @param [finalName] the final name of the file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun configureUploadRequestBody(httpRB: HttpRequestBuilder, encryptedData: ByteArray,
                                       finalName: String) {
        httpRB.setBody(MultiPartFormDataContent(
            formData {
                append("description", "zipped files")
                append("zip", encryptedData, Headers.build {
                    append(HttpHeaders.ContentType, "application/zip")
                    append(HttpHeaders.ContentDisposition, "filename=\"$finalName\"")
                })
            },
            boundary = "WebAppBoundary"
        ))
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
     * Zips files contained in the source file
     * @param [zipOut] the zip output stream
     * @param [sourceFile] the File to zip
     * @param [parentDirPath] the path of the sourceFile's parent
     * code snippet from https://www.folkstalk.com/tech/how-to-zip-folders-subfolders-with-files-in-it-in-kotlin-using-zipoutputstream-with-code-solution/
     */
    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val data = ByteArray(2048)
        for (file in sourceFile.listFiles()!!) {
            if(file.isDirectory) {
                ifZipDirectory(file, zipOut)
            } else {
                if (!file.name.contains(".zip") && !file.name.contains("path.txt")) {
                    writeEntryArchive(file, parentDirPath, zipOut, data)
                } else {
                    zipOut.closeEntry()
                    zipOut.close()
                }
            }
        }
    }

    /**
     * Manages when zipFiles meet a directory
     * @param [file] the file in the sourceFile list
     * @param [zipOut] the zip output stream
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun ifZipDirectory(file: File, zipOut: ZipOutputStream) {
        val path = file.name+File.separator
        setEntry(file, zipOut, path)
        Log.i("zip", "Adding Directory: " + file.name)
        zipFiles(zipOut, file, file.name)
    }

    /**
     * Prepare the entry to be added to the zipOutputStream
     * @param [file] the next entry
     * @param [zipOut] the zipOutputStream
     * @param [path] the path of the file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun setEntry(file: File, zipOut: ZipOutputStream, path: String) {
        val entry = ZipEntry(path)
        entry.time = file.lastModified()
        entry.isDirectory
        entry.size = file.length()
        zipOut.putNextEntry(entry)
    }

    /**
     * Write the data of the entry in the zip archive
     * @param [file] the next entry
     * @param [parent] the parent path of the file
     * @param [zipOut] the zipOutputStream
     * @param [data] the ByteArray buffer
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun writeEntryArchive(file: File, parent: String, zipOut: ZipOutputStream,
                                  data: ByteArray) {
        FileInputStream(file).use { fileI ->
            BufferedInputStream(fileI).use { origin ->
                val path = parent + File.separator + file.name
                Log.i("zip", "Adding file: $path")
                setEntry(file, zipOut, path)
                while (true) {
                    val readBytes = origin.read(data)
                    if (readBytes == -1) break
                    zipOut.write(data, 0, readBytes)
                }
            }
        }
    }

    /**
     * Get self phone number
     * @return the phone number as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 26-01-2023)
     */
    private fun getPhoneNumber(): String {
        if (ActivityCompat.checkSelfPermission(this@Work2Activity,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 3)
        }
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.line1Number
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                checkForLocation()
        } else if (requestCode == 3) {
            getPhoneNumber()
        } else {
            Toast.makeText(this, "Location permission was denied", Toast.LENGTH_SHORT).show()
        }
        return //see diff if we take that off
    }

    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {}

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}