package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.TextView
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
import com.projet.sluca.smallbrother.utils.*

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

/**
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 03-06-2023)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    val vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var tvAction: TextView
    private var battery: String? = null

    private lateinit var pictureService: APictureCapturingService

    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener

    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private var address1: String = ""
    private var address2: String = ""
    private var addressDiff: Boolean = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var zipName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        userData = UserDataManager.getUserData(application)

        val tvLoading = findViewById<TextView>(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""
        findViewById<TextView>(R.id.travail).text = getString(R.string.message06)
            .replace("§%", userData.nomPartner)

        loading(tvLoading)
        setAppBarTitle(userData, this)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // --> [2] Get Aide Location
        if(locationAvailability()) {

            locationListener = object: LocationListener {
                override fun onLocationChanged(newLoc: Location) {
                    when {
                        hasGps -> locationGps = newLoc
                        hasNetwork -> locationNetwork = newLoc
                    }
                }
                override fun onProviderEnabled(provider: String) = getLocation()
                override fun onProviderDisabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            requestPermission()
            @Suppress("MissingPermission")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 1f, locationListener
            )

            tvAction.text = getString(R.string.message12C)
            checkForLocation()
            if(locationGps==null && locationNetwork==null) getLocation()
            startAddressesTimer()
        } else takePictures()
    }

    /**
     * Starts a countdown timer to get address 10 seconds apart
     */
    private fun startAddressesTimer() {
        object : CountDownTimer(14000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                when (millisUntilFinished) {
                    in 11000..13000 -> address1 = getAddress()
                    in 1000..3000 -> address2 = getAddress()
                }
            }
            override fun onFinish() {
                if(address1 != "" && address2 != "") addressDiff = compareAddresses()
                locationManager.removeUpdates(locationListener)
                takePictures()
            }
        }.start()
    }

    /**
     * Checks if addresses match
     */
    private fun compareAddresses(): Boolean = !(address1.contentEquals(address2))


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
        val acceleration = if(intent.hasExtra("accInterpretation"))
            intent.getStringExtra("accInterpretation").toString() else "Indéterminé"
        val xyz: Boolean = if(intent.hasExtra("movementInterpretation"))
            intent.getBooleanExtra("movementInterpretation", true) else true

        val movementDataInterpretation = interpretMotionData(acceleration, xyz, addressDiff)

        // --> [7] Get light level
        val light = if(intent.hasExtra("light")) intent.getStringExtra("light") else ""

        tvAction.text = getString(R.string.message12F)

        val nomAide = userData.nom
        val particule = particule(nomAide)

        object : Thread() {
            override fun run() {
                try {
                    val client = initClient()
                    val location: String = if((locationGps != null || locationNetwork != null)
                        && locationAvailability()){
                        getAddress()
                    } else "Erreur lors de la récupération de la position"

                    val currentTime = getCurrentTime("dd/MM/yyyy HH:mm:ss")
                    val information = "Localisation $particule$nomAide : $location\n" +
                            "Coordonnées géographiques: ${locationGps?.latitude}, " +
                            "${locationGps?.longitude}\n" +
                            "Niveau de batterie : $battery\n" +
                            "En mouvement ? : $acceleration.\n" +
                            "Interprétation mouvement : $movementDataInterpretation\n" +
                            "Niveau de lumiere (en lux) : $light.\n" +
                            "Date de la capture : $currentTime\n"
                    Log.d("infos", information)

                    val informationFile = File(userData.path + "/SmallBrother/informations.txt")
                    writeInfoInFile(informationFile, information)

                    val ziPath = this@Work2Activity.filesDir.path+"/SmallBrother/zippedFiles.zip"

                    zipAll(this@Work2Activity.filesDir.path+"/SmallBrother", ziPath)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (File(ziPath).exists()) zipName = uploadZip(client, File(ziPath))
                            resolveUpload()

                            audioFile.delete()
                            firstPicture.delete()
                            secondPicture.delete()
                            informationFile.delete()
                            val zipFile = File(ziPath)
                            zipFile.delete()
                            Log.i("EOU", "upload ended")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                locationManager.removeUpdates(locationListener)
                vibreur.vibration(this@Work2Activity, 330)

                activateSMSReceiver(this@Work2Activity)

                val intent = Intent(this@Work2Activity, AideActivity::class.java)
                startActivity(intent)
            }
        }.start()
    }

    /**
     * Send correct message based on upload result
     */
    private fun resolveUpload() {
        if(zipName != "") {
            val fileLocMsg = getString(R.string.smsys10)
                .replace("§%", "$URLServer/download/$zipName")

            sendSMS(this@Work2Activity, fileLocMsg, userData.telephone, vibreur)
            userData.refreshLog(21)
        } else {
            sendSMS(
                this@Work2Activity,
                getString(R.string.smsys09),
                userData.telephone,
                vibreur
            )
            userData.refreshLog(23)
        }
    }

    /**
     * Writes information collected in the file
     * @param informationFile the File
     * @param information the info to write
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-06-2023)
     */
    private fun writeInfoInFile(informationFile: File, information: String) {
        informationFile.createNewFile()
        val outputStream = FileOutputStream(informationFile)
        val bufferedWriter = BufferedWriter(OutputStreamWriter(
            outputStream,
            Charsets.UTF_8
        ))
        bufferedWriter.write(information)
        bufferedWriter.close()
    }

    /**
     * Take a picture with front and back camera if possible
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 06-04-2023)
     */
    private fun takePictures() {
        tvAction.text = getString(R.string.message12B)
        pictureService = PictureCapturingServiceImpl.getInstance(this@Work2Activity)
        pictureService.startCapturing(this@Work2Activity, this@Work2Activity)

        onBackPressedDispatcher.addCallback(this@Work2Activity, onBackPressedCallback)
    }

    /**
     * function to get Location of Aide's phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 26-02-2023)
     */
    @SuppressLint("MissingPermission")
    private fun checkForLocation() {
        requestPermission()
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(!hasGps && !hasNetwork) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        assert(hasGps || hasNetwork)
    }

    /**
     * Retrieve location as a Location object inside locationGps or locationNetwork
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 18-03-2023)
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationGps = null
        locationNetwork = null
        Log.d("Address 1 in getLocation", address1)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && hasGps) locationGps = location
            else if(location != null && hasNetwork) {
                fusedLocationClient.lastLocation.addOnSuccessListener { networkLocation ->
                    locationNetwork = networkLocation
                }.addOnFailureListener { e ->
                    Log.e("Work2Activity","Error getting network location", e)
                }
            } else requestNewLocationData()
        }.addOnFailureListener { e -> Log.e("Work2Activity", "Error getting GPS location", e) }
    }

    /**
     * function to get an address from a Location object
     * @return the address as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun getAddress() : String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address: Address? = when {
            locationGps != null -> getGpsAddress(geoCoder)
            locationNetwork != null -> getNetworkAddress(geoCoder)
            else -> null
        }
        return address?.getAddressLine(0).toString()
    }

    /**
     * Get the Address object based on GPS geographic coordinates using Geocoder class
     * @param geoCoder the Geocoder object
     * @return the address or null if none recovered
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun getGpsAddress(geoCoder: Geocoder): Address? {
        var address: Address? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(locationGps!!.latitude, locationGps!!.longitude, 1)
            { results -> if (results.isNotEmpty()) address = results[0] }
        } else {
            @Suppress("DEPRECATION")
            address = geoCoder
                .getFromLocation(locationGps!!.latitude, locationGps!!.longitude, 1)?.first()
        }
        return address
    }

    /**
     * Get the Address object based on Network geographic coordinates using Geocoder class
     * @param geoCoder the Geocoder object
     * @return the address or null if none recovered
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun getNetworkAddress(geoCoder: Geocoder): Address? {
        var address: Address? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(locationNetwork!!.latitude, locationNetwork!!.longitude, 1)
            { results -> if (results.isNotEmpty()) address = results[0] }
        } else {
            @Suppress("DEPRECATION")
            address = geoCoder
                .getFromLocation(locationNetwork!!.latitude, locationNetwork!!.longitude, 1)
                ?.first()
        }
        return address
    }

    /**
     * request new location data if no previous one existing
     * @author Maxime Caucheteur (from https://www.androidhire.com/current-location-in-android-using-kotlin/)
     * @version 1.2 (Updated on 16-03-23)
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        requestPermission()
        val locationRequest = LocationRequest()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.numUpdates = 1
        if(!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationGps = locationResult.lastLocation
            locationNetwork = locationResult.lastLocation
        }
    }

    /**
     * renames the zip file and uploads it and the AideData object to the server
     * @param [client] the HttpClient used to access the server
     * @param [file] the zip file to upload
     * @return the final name of the file to put in the download URL
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 03-06-2023)
     */
    private suspend fun uploadZip(client: HttpClient, file: File): String {
        val finalName = generateRandomName()
        val aesKey = SecurityUtils.getAESKey()
        val iv = SecurityUtils.generateIVForAES()
        val encryptedData = SecurityUtils.encryptDataAes(file.readBytes(), aesKey, iv)
        val signature = SecurityUtils.signFile(encryptedData)
        val signString = android.util.Base64.encodeToString(signature, android.util.Base64.NO_WRAP)
        val aesEncKey = encryptAESKeyWithRSA(aesKey)
        uploadFileRequest(client, encryptedData, finalName)
        val aideData = AideData(finalName,aesEncKey, signString,
            android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT))
        uploadAideDataRequest(client, aideData)
        client.close()
        return finalName
    }

    /**
     * Generates a random name with randomUUID generator of a size of nameSize.
     * nameSize must fit the SMSReceiver subsequence to check url to file.
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
     * Uploads the AideData object with all the information needed
     * @param client the HttpClient
     * @param aideData the AideData object
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 14-04-2023)
     */
    private suspend fun uploadAideDataRequest(client: HttpClient, aideData: AideData) {
        client.post("$URLServer/upload/aideData") {
            contentType(ContentType.Application.Json)
            setBody(aideData)
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

    /**
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

    /**
     * Zips files contained in the source file
     * @param [zipOut] the zip output stream
     * @param [sourceFile] the File to zip
     * code snippet from https://www.folkstalk.com/tech/how-to-zip-folders-subfolders-with-files-in-it-in-kotlin-using-zipoutputstream-with-code-solution/
     */
    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val data = ByteArray(2048)
        for (file in sourceFile.listFiles()!!) {
            if(file.isDirectory) {
                ifZipDirectory(file, zipOut)
            } else {
                if (!file.name.endsWith(".zip") && file.name != "donnees.txt") {
                    writeEntryArchive(file, parentDirPath, zipOut, data)
                }
            }
        }
        zipOut.closeEntry()
        zipOut.close()
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
     * Request permissions to retrieve locations
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 26-02-2023)
     */
    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                checkForLocation()
        }
        else if (requestCode == 2) recreate()
        else message(this, "La localisation n'est pas activée", vibreur)
        return
    }


    /* ------------- Functions for location permissions ---------------- */

    /**
     * Checks if the location service is enabled
     * @return true if at least one service is available
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 05-04-2023)
     */
    private fun locationAvailability(): Boolean {
        requestLocationPermission()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return (hasGps || hasNetwork)
    }

    /**
     * Requests the location permission if it is not granted
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 27-04-2023)
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ), 2)
        }
    }

    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {}

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}