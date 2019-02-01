package com.pingerx.socialgo.core.adapter.impl

import android.annotation.SuppressLint
import android.text.TextUtils
import com.pingerx.socialgo.core.adapter.IRequestAdapter
import com.pingerx.socialgo.core.utils.SocialGoUtils
import com.pingerx.socialgo.core.utils.SocialLogUtils
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import javax.net.ssl.*

/**
 * 默认的网络请求，使用HttpURLConnection
 */
open class DefaultRequestAdapter : IRequestAdapter {

    private var mConnection: HttpURLConnection? = null

    override fun getFile(url: String): File? {
        if (TextUtils.isEmpty(url) || !url.startsWith("http"))
            return null
        var file: File? = null
        try {
            file = File(SocialGoUtils.mapUrl2LocalPath(url))
            if (!SocialGoUtils.isExist(file)) {
                return SocialGoUtils.saveStreamToFile(file, openStream(url, isHttps(url)))
            }
        } catch (e: Exception) {
            SocialLogUtils.e(e)
        } finally {
            close()
        }
        return file
    }

    override fun getJson(url: String): String? {
        if (TextUtils.isEmpty(url) || !url.startsWith("http"))
            return null
        try {
            return SocialGoUtils.saveStreamToString(openStream(url, isHttps(url)))
        } catch (e: Exception) {
            SocialLogUtils.e(e)
        } finally {
            close()
        }
        return null
    }

    override fun postData(url: String, params: Map<String, String>, fileKey: String, filePath: String): String? {
        throw RuntimeException("如果想要支持 openApi, 则需要实现该方法，由于使用 HttpUrlConn 实现太复杂，建议使用 OkHttpClient 实现")
    }

    private fun isHttps(url: String): Boolean {
        return url.startsWith("https")
    }

    private fun close() {
        mConnection?.disconnect()
    }

    @Throws(Exception::class)
    private fun openStream(url: String, isHttps: Boolean): InputStream {
        mConnection = URL(url).openConnection() as HttpURLConnection
        if (isHttps) {
            initHttpsConnection(mConnection)
        }
        return SocialGoUtils.openGetHttpStream(mConnection!!)
    }

    /**
     * 针对 https 连接配置
     */
    @Throws(Exception::class)
    private fun initHttpsConnection(conn: HttpURLConnection?) {
        val httpsURLConnection = conn as? HttpsURLConnection?
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf<TrustManager>(DefaultTrustManager()), SecureRandom())
        httpsURLConnection?.sslSocketFactory = sc.socketFactory
        httpsURLConnection?.hostnameVerifier = HostnameVerifier { hostname, _ -> hostname != null }
    }


    @SuppressLint("TrustAllX509TrustManager")
    private inner class DefaultTrustManager : X509TrustManager {

        @Throws(java.security.cert.CertificateException::class)
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {

        }

        @Throws(java.security.cert.CertificateException::class)
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {

        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
            return null
        }
    }
}
