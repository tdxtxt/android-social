package com.fungo.socialgo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Pinger
 * @since 2018/7/1 下午3:19
 * 社会化分享和支付工具类
 */
object SocialUtils {

    //============ 网络 =============
    /**
     * 当前的网络状态
     */
    private const val NO_NETWORK = 0
    private const val NETWORK_WIFI = 3
    private const val NETWORK_MOBILE = 4

    /**
     * 判断当前网络类型  0为没有网络连接  3为WiFi 4为2G5为3G6为4G
     */
    private fun getNetworkType(context: Context): Int {
        //改为context.getApplicationContext()，防止在Android 6.0上发生内存泄漏
        val connectMgr = context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectMgr.activeNetworkInfo
                ?: return NO_NETWORK // 没有任何网络
        if (!networkInfo.isConnected) {
            return NO_NETWORK  // 网络断开或关闭
        }
        if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
            // wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接
            return NETWORK_WIFI
        } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
            // 移动数据连接,不能与连接共存,如果wifi打开，则自动关闭
            when (networkInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN,
                    // 2G网络
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP,
                    // 3G网络
                TelephonyManager.NETWORK_TYPE_LTE ->
                    // 4G网络
                    return NETWORK_MOBILE
            }
        }
        return NO_NETWORK
    }


    fun isWifiNetWork(context: Context): Boolean {
        return getNetworkType(context) == NETWORK_WIFI
    }

    fun isMobileNetWork(context: Context): Boolean {
        return getNetworkType(context) == NETWORK_MOBILE
    }

    fun isNetWorkAvailable(context: Context): Boolean {
        return getNetworkType(context) != NO_NETWORK
    }




    // ============== 日志 =============

    fun e(msg: String) {
        println(msg)
    }

    fun e(tag: String, msg: String) {
        println("$tag：$msg")
    }


    // ============= 图片处理 =============
    /** 在保证质量的情况下尽可能压缩 不保证压缩到指定字节 */
    fun compressBitmap(datas: ByteArray?, byteCount: Int): ByteArray? {
        var isFinish = false
        if (datas != null && datas.size > byteCount) {
            val outputStream = ByteArrayOutputStream()
            val tmpBitmap = BitmapFactory.decodeByteArray(datas, 0, datas.size)
            var times = 1
            var percentage = 1.0

            while (!isFinish && times <= 10) {
                percentage = Math.pow(0.8, times.toDouble())
                val compress_datas = (100.0 * percentage).toInt()
                tmpBitmap.compress(Bitmap.CompressFormat.JPEG, compress_datas, outputStream)
                if (outputStream.size() < byteCount) {
                    isFinish = true
                } else {
                    outputStream.reset()
                    ++times
                }
            }
            val outputStreamByte = outputStream.toByteArray()
            if (!tmpBitmap.isRecycled) {
                tmpBitmap.recycle()
            }
            if (outputStreamByte.size > byteCount) {
                e("BitmapUtils", "compressBitmap cannot compress to " + byteCount + ", after compress size=" + outputStreamByte.size)
            }
            return outputStreamByte
        }
        return datas
    }



    /** Bitmap 转 bytes */
    fun bitmap2Bytes(bitmap: Bitmap?): ByteArray? {
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        if (bitmap != null && !bitmap.isRecycled) {
            try {
                byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                if (byteArrayOutputStream.toByteArray() == null) {
                    e("BitmapUtils", "bitmap2Bytes byteArrayOutputStream toByteArray=null")
                }
                return byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                e("BitmapUtils", e.toString())
            } finally {
                try {
                    byteArrayOutputStream?.close()
                } catch (var14: IOException) {
                }
            }
            return null
        } else {
            e("BitmapUtils", "bitmap2Bytes bitmap == null or bitmap.isRecycled()")
            return null
        }
    }

    /** Bitmap保存为文件 */
    fun saveBitmapFile(bitmap: Bitmap, path: String): File? {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(path)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return File(path)
    }


    // ================= 线程切换 ===================
    private val mHandler: Handler = Handler(Looper.getMainLooper())



    /** 运行在主线程 */
    fun runOnUIThread(run: Runnable) {
        mHandler.post(run)
    }

    /** 运行在子线程 */
    fun runOnSubThread(run: Runnable) {
        singlePool.execute(run)
    }

    /** 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题  */
    val singlePool: ExecutorService
        get() = Executors.newSingleThreadExecutor()



    // ================== Json相关 ====================
    fun jsonToMap(obj: JSONObject): Map<String, String> {
        val map = HashMap<String, String>()
        val iterator = obj.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = obj.opt(key).toString()
        }
        return map
    }

}