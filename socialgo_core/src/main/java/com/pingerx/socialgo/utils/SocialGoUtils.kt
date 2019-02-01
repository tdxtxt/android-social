package com.pingerx.socialgo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.pingerx.socialgo.SocialGo
import com.pingerx.socialgo.exception.SocialError
import java.io.*
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author Pinger
 * @since 下午3:41 下午3:41
 *
 *
 * 社会化登录分享工具类
 */
object SocialGoUtils {

    private const val THUMB_IMAGE_SIZE = 32 * 1024
    private const val SHARE_REQ_CODE = 0x123
    private const val POINT_GIF = ".gif"
    private const val POINT_JPG = ".jpg"
    private const val POINT_JPEG = ".jpeg"
    private const val POINT_PNG = ".png"


    // -------------------------- 分享相关-------------------------------
    // -------------------------- 分享相关-------------------------------
    // -------------------------- 分享相关-------------------------------

    fun shareText(activity: Activity, title: String, text: String, pkg: String, targetActivity: String): Boolean {
        val sendIntent = Intent()
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.putExtra(Intent.EXTRA_TITLE, title)
        sendIntent.type = "text/plain"
        return activeShare(activity, sendIntent, pkg, targetActivity)
    }


    fun shareVideo(activity: Activity, path: String, pkg: String, targetActivity: String): Boolean {
        //由文件得到uri
        val videoUri = Uri.fromFile(File(path))
        val shareIntent = Intent()
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri)
        shareIntent.type = "video/*"
        printActivitySupport(activity, shareIntent)
        return activeShare(activity, shareIntent, pkg, targetActivity)
    }


    private fun activeShare(activity: Activity, sendIntent: Intent, pkg: String, targetActivity: String): Boolean {
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (!TextUtils.isEmpty(targetActivity))
            sendIntent.setClassName(pkg, targetActivity)
        try {
            val chooserIntent = Intent.createChooser(sendIntent, "请选择") ?: return false
            activity.startActivityForResult(chooserIntent, SHARE_REQ_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }


    private fun printActivitySupport(activity: Activity, intent: Intent) {
        val resolveInfos = activity.packageManager
                .queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
        for (resolveInfo in resolveInfos) {
            SocialLogUtils.e(resolveInfo.activityInfo.packageName + " - " + resolveInfo.activityInfo.name)
        }
    }


    // -------------------------- 图片相关-------------------------------
    // -------------------------- 图片相关-------------------------------
    // -------------------------- 图片相关-------------------------------


    class Size(var width: Int, var height: Int)

    /**
     * 根据kb计算缩放后的大约宽高
     *
     * @param originSize 图片原始宽高
     * @param maxSize    byte length
     * @return 大小
     */
    private fun calculateSize(originSize: Size, maxSize: Int): Size {
        val bw = originSize.width
        val bh = originSize.height
        // 如果本身已经小于，就直接返回
        if (bw * bh <= maxSize) {
            return Size(bw, bh)
        }
        // 拿到大于1的宽高比
        var isHeightLong = true
        var bitRatio = bh * 1f / bw
        if (bitRatio < 1) {
            bitRatio = bw * 1f / bh
            isHeightLong = false
        }
        // 较长边 = 较短边 * 比例(>1)
        // maxSize = 较短边 * 较长边 = 较短边 * 较短边 * 比例(>1)
        // 由此计算短边应该为 较短边 = sqrt(maxSize/比例(>1))
        val thumbShort = Math.sqrt((maxSize / bitRatio).toDouble()).toInt()
        // 较长边 = 较短边 * 比例(>1)
        val thumbLong = (thumbShort * bitRatio).toInt()
        return if (isHeightLong) {
            Size(thumbShort, thumbLong)
        } else {
            Size(thumbLong, thumbShort)
        }
    }

    /**
     * 获取图片大小
     *
     * @param filePath 路径
     * @return Size
     */
    private fun getBitmapSize(filePath: String): Size {
        // 仅获取宽高
        val options = BitmapFactory.Options()
        // 该属性设置为 true 只会加载图片的边框进来，并不会加载图片具体的像素点
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        // 获得原图的宽和高
        val outWidth = options.outWidth
        val outHeight = options.outHeight
        return Size(outWidth, outHeight)
    }

    /**
     * 使用 path decode 出来一个差不多大小的，此时因为图片质量的关系，可能大于kbNum
     *
     * @param filePath path
     * @param maxSize  byte
     * @return bitmap
     */
    private fun getMaxSizeBitmap(filePath: String, maxSize: Int): Bitmap {
        val originSize = getBitmapSize(filePath)
        SocialLogUtils.e("原始图片大小 = " + originSize.width + " * " + originSize.height)
        var sampleSize = 0
        // 我们对较小的图片不进行采样，因为采样只是尽量接近 32k 和避免占用大量内存
        // 对较小图片进行采样会导致图片更模糊，所以对不大的图片，直接走后面的细节调整
        if (originSize.height * originSize.width < 400 * 400) {
            sampleSize = 1
        } else {
            val size = calculateSize(originSize, maxSize * 5)
            SocialLogUtils.e("目标图片大小 = " + size.width + " * " + size.height)
            while (sampleSize == 0
                    || originSize.height / sampleSize > size.height
                    || originSize.width / sampleSize > size.width) {
                sampleSize += 2
            }
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        options.inMutable = true
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        SocialLogUtils.e("sample size = " + sampleSize + " 采样后 bitmap大小 = " + bitmap.byteCount)
        return bitmap
    }


    /**
     * 创建指定大小的bitmap的byte流，大小 <= maxSize
     *
     * @param srcBitmap bitmap
     * @param maxSize   kb,example 32kb
     * @return byte流
     */
    private fun getStaticSizeBitmapByteByBitmap(srcBitmap: Bitmap, maxSize: Int, format: Bitmap.CompressFormat): ByteArray? {
        var bitmap = srcBitmap
        // 首先进行一次大范围的压缩
        var tempBitmap: Bitmap
        val output = ByteArrayOutputStream()
        // 设置矩阵数据
        val matrix = Matrix()
        bitmap.compress(format, 100, output)
        // 如果进行了上面的压缩后，依旧大于32K，就进行小范围的微调压缩
        var bytes = output.toByteArray()
        SocialLogUtils.e("开始循环压缩之前 bytes = " + bytes.size)
        while (bytes.size > maxSize) {
            matrix.setScale(0.9f, 0.9f)//每次缩小 1/10
            tempBitmap = bitmap
            bitmap = Bitmap.createBitmap(
                    tempBitmap, 0, 0,
                    tempBitmap.width, tempBitmap.height, matrix, true)
            recyclerBitmaps(tempBitmap)
            output.reset()
            bitmap.compress(format, 100, output)
            bytes = output.toByteArray()
            SocialLogUtils.e("压缩一次 bytes = " + bytes.size)
        }
        SocialLogUtils.e("压缩后的图片输出大小 bytes = " + bytes.size)
        recyclerBitmaps(bitmap)
        return bytes
    }

    /**
     * 根据路径获取指定大小的图片
     * @param path    路径
     * @return byte[]
     */
    fun getStaticSizeBitmapByteByPath(path: String): ByteArray? {
        val srcBitmap = getMaxSizeBitmap(path, THUMB_IMAGE_SIZE)
        var format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        if (isPngFile(path)) format = Bitmap.CompressFormat.PNG
        return getStaticSizeBitmapByteByBitmap(srcBitmap, THUMB_IMAGE_SIZE, format)
    }


    private fun recyclerBitmaps(vararg bitmaps: Bitmap?) {
        try {
            for (bitmap in bitmaps) {
                if (bitmap != null && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 文件后缀
     *
     * @param path 路径
     * @return 后缀名
     */
    private fun getSuffix(path: String): String {
        if (!TextUtils.isEmpty(path)) {
            val lineIndex = path.lastIndexOf("/")
            if (lineIndex != -1) {
                val fileName = path.substring(lineIndex, path.length)
                if (!TextUtils.isEmpty(fileName)) {
                    val pointIndex = fileName.lastIndexOf(".")
                    if (pointIndex != -1) {
                        val suffix = fileName.substring(pointIndex, fileName.length)
                        if (!TextUtils.isEmpty(suffix)) {
                            return suffix
                        }
                    }

                }
            }
        }
        return ""
    }


    // -------------------------- 文件相关-------------------------------
    // -------------------------- 文件相关-------------------------------
    // -------------------------- 文件相关-------------------------------

    /**
     * @param path 路径
     * @return 是否是 gif 文件
     */
    fun isGifFile(path: String): Boolean {
        return path.toLowerCase().endsWith(POINT_GIF)
    }

    /**
     * @param path 路径
     * @return 是不是 jpg || png
     */
    private fun isJpgPngFile(path: String): Boolean {
        return isJpgFile(path) || isPngFile(path)
    }

    /**
     * @param path 路径
     * @return 是不是 jpg 文件
     */
    private fun isJpgFile(path: String): Boolean {
        return path.toLowerCase().endsWith(POINT_JPG) || path.toLowerCase().endsWith(POINT_JPEG)
    }

    /**
     * @param path 路径
     * @return 是不是 png 文件
     */
    fun isPngFile(path: String): Boolean {
        return path.toLowerCase().endsWith(POINT_PNG)
    }

    /**
     * @param path 路径
     * @return 是不是 图片 文件
     */
    fun isPicFile(path: String): Boolean {
        return isJpgPngFile(path) || isGifFile(path)
    }

    /**
     * @param path 路径
     * @return 文件是否存在
     */
    fun isExist(path: String?): Boolean {
        if (TextUtils.isEmpty(path))
            return false
        val file = File(path)
        return file.exists() && file.length() > 0
    }

    /**
     * @param file 文件
     * @return 文件是否存在
     */
    fun isExist(file: File?): Boolean {
        return file != null && isExist(file.absolutePath)
    }

    /**
     * @param path 路径
     * @return 是不是 http 路径
     */
    fun isHttpPath(path: String?): Boolean {
        return path?.toLowerCase()?.startsWith("http") ?: false
    }

    /**
     * 网络路径映射本地路径
     *
     * @param url 网络路径
     * @return 映射的本地路径
     */
    fun mapUrl2LocalPath(url: String): String {
        // 映射文件名
        var suffix = getSuffix(url)
        suffix = if (TextUtils.isEmpty(suffix)) ".png" else suffix
        val fileName = getMD5(url) + suffix
        val saveFile = File(SocialGo.getConfig().getCacheDir(), fileName)
        return saveFile.absolutePath
    }


    /**
     * 将资源图片映射到本地文件存储，同一张图片不必重复decode
     *
     * @param context ctx
     * @param resId   资源ID
     * @return 路径
     */
    fun mapResId2LocalPath(context: Context, resId: Int): String? {
        val fileName = getMD5(resId.toString() + "") + POINT_PNG
        val saveFile = File(SocialGo.getConfig().getCacheDir(), fileName)
        if (saveFile.exists())
            return saveFile.absolutePath
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeResource(context.resources, resId)
            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(saveFile))
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } finally {
            recyclerBitmaps(bitmap)
        }
        return saveFile.absolutePath
    }

    // -------------------------- 数据流相关-------------------------------
    // -------------------------- 数据流相关-------------------------------
    // -------------------------- 数据流相关-------------------------------
    // 关闭流
    fun closeStream(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            try {
                closeable?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打开一个网络流
     *
     * @param conn 网络连接
     * @return 流
     * @throws IOException error
     */
    @Throws(IOException::class)
    fun openGetHttpStream(conn: HttpURLConnection): InputStream {
        conn.requestMethod = "GET"
        conn.readTimeout = 3000
        conn.connectTimeout = 3000
        conn.doInput = true
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*")
        conn.setRequestProperty("connection", "Keep-Alive")
        // 发起连接
        conn.connect()
        return conn.inputStream
    }


    /**
     * 保存文件到
     *
     * @param file 文件
     * @param stream   流
     * @return 文件
     */
    fun saveStreamToFile(file: File, stream: InputStream): File? {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        val bs: ByteArray?
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            bis = BufferedInputStream(stream)
            bos = BufferedOutputStream(FileOutputStream(file))
            bs = ByteArray(1024 * 10)
            var len: Int = bis.read(bs)
            while (len != -1) {
                bos.write(bs, 0, len)
                bos.flush()
                len = bis.read(bs)
            }
            bis.close()
            bos.close()
        } catch (e: Exception) {
            SocialLogUtils.t(e)
            return null
        } finally {
            closeStream(bis, bos)
        }
        return file
    }


    /**
     * 从流中读取为字符串
     *
     * @param stream 流
     * @return json
     */
    fun saveStreamToString(stream: InputStream): String? {
        var br: BufferedReader? = null
        var json: String? = null
        try {
            br = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String? = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }
            json = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeStream(br)
        }
        return json
    }


    // -------------------------- Json相关-------------------------------
    // -------------------------- Json相关-------------------------------
    // -------------------------- Json相关-------------------------------


    interface Callback<T> {

        fun onSuccess(data: T?)

        fun onFailure(e: SocialError)
    }

    fun <T> getObject(json: String?, cls: Class<T>): T? {
        if (json != null) {
            val jsonAdapter = SocialGo.getJsonAdapter()
            try {
                return jsonAdapter.fromJson(json, cls)
            } catch (e: Exception) {
                SocialLogUtils.e(e)
            }
        }
        return null
    }

    fun getObject2Json(any: Any?): String? {
        if (any != null) {
            val jsonAdapter = SocialGo.getJsonAdapter()
            try {
                return jsonAdapter.toJson(any)
            } catch (e: Exception) {
                SocialLogUtils.e(e)
            }
        }
        return null
    }


    fun <T> startJsonRequest(url: String, clz: Class<T>, callback: Callback<T>) {
        SocialGo.getExecutor().execute {
            try {
                var any: T? = null
                val json = SocialGo.getRequestAdapter().getJson(url)
                if (!TextUtils.isEmpty(json)) {
                    any = getObject(json, clz)
                }
                SocialGo.getHandler().post {
                    if (any != null) {
                        callback.onSuccess(any)
                    } else {
                        callback.onFailure(SocialError(SocialError.CODE_PARSE_ERROR, "json 无法解析"))
                    }
                }
            } catch (e: Exception) {
                SocialGo.getHandler().post {
                    callback.onFailure(SocialError(SocialError.CODE_COMMON_ERROR, e))
                }
            }
        }
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // 任何一个为空 返回true
    fun isAnyEmpty(vararg strings: String?): Boolean {
        var isEmpty = false
        for (string in strings) {
            if (TextUtils.isEmpty(string)) {
                isEmpty = true
                break
            }
        }
        return isEmpty
    }

    // app 是否安装
    fun isAppInstall(context: Context, pkgName: String): Boolean {
        val pm = context.packageManager ?: return false
        val packages = pm.getInstalledPackages(0)
        var result = false
        for (info in packages) {
            if (TextUtils.equals(info.packageName.toLowerCase(), pkgName)) {
                result = true
                break
            }
        }
        return result
    }

    // 根据包名，打开对应app
    fun openApp(context: Context, pkgName: String?): Boolean {
        return try {
            if (pkgName != null) {
                val intent = context.packageManager.getLaunchIntentForPackage(pkgName)
                context.startActivity(intent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }


    // 获取 md5
    fun getMD5(info: String): String {
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(info.toByteArray(charset("UTF-8")))
            val encryption = md5.digest()
            val strBuf = StringBuilder()
            for (anEncryption in encryption) {
                if (Integer.toHexString(0xff and anEncryption.toInt()).length == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff and anEncryption.toInt()))
                } else {
                    strBuf.append(Integer.toHexString(0xff and anEncryption.toInt()))
                }
            }
            return strBuf.toString()
        } catch (e: NoSuchAlgorithmException) {
            return info
        } catch (e: UnsupportedEncodingException) {
            return info
        }
    }
}
