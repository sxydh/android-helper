package cn.net.bhe.androidhelper.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.InetAddress

object IPUtils {

    fun getLanIP(context: Context): String? {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return null
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null

        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
            val linkAddresses = linkProperties?.linkAddresses
            linkAddresses?.forEach { linkAddress ->
                if (linkAddress.address is InetAddress &&
                    !linkAddress.address.isLoopbackAddress &&
                    linkAddress.address.address.size == 4
                ) {
                    return linkAddress.address.hostAddress
                }
            }
        }
        return null
    }

}