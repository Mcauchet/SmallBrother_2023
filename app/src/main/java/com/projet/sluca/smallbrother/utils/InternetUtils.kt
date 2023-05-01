package com.projet.sluca.smallbrother.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException

/**
 * Returns true if device has validated network capabilities (Cellular, Wifi or Ethernet)
 * @param context the context of the application
 * @return true if connected, false otherwise
 * @author Maxime Caucheteur (inspired by https://medium.com/@veniamin.vynohradov/monitoring-internet-connection-state-in-android-da7ad915b5e5)
 * @version 1.2 (Updated on 04-01-2023)
 */
fun isOnline(context: Context): Boolean {
    try {
        return checkInternetCapabilities(context)
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}

/**
 * Checks Network Capabilities
 * @param [context] the context of the activity
 * @return true if has Network capabilities, false otherwise
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
private fun checkInternetCapabilities(context: Context) : Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?: return false
    return when {
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        -> true
        else -> false
    }
}