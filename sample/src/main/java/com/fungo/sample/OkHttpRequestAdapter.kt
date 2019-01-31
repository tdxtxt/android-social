package com.fungo.sample

import android.text.TextUtils
import com.fungo.socialgo.adapter.impl.DefaultRequestAdapter
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 使用Okhttp发起请求
 */
class OkHttpRequestAdapter : DefaultRequestAdapter() {

    private val mOkHttpClient: OkHttpClient = buildOkHttpClient()

    private fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        // 连接超时
        builder.connectTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        // 读超时
        builder.readTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        // 写超时
        builder.writeTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        // 失败后重试
        builder.retryOnConnectionFailure(true)
        return builder.build()
    }


    // 借助 open api 提交图片
    override fun postData(url: String, params: Map<String, String>, fileKey: String, filePath: String): String? {
        val builder = MultipartBody.Builder()
        if (!TextUtils.isEmpty(filePath)) {
            val file = File(filePath)
            val body = RequestBody.create(MediaType.parse("image/*"), file)
            builder.addFormDataPart(fileKey, file.name, body)
            builder.setType(MultipartBody.FORM)
            for (key in params.keys) {
                val value = params[key]
                if (value != null) {
                    builder.addFormDataPart(key, value)
                }
            }
            val multipartBody = builder.build()
            val request = Request.Builder().url(url).post(multipartBody).build()
            return try {
                val execute = mOkHttpClient.newCall(request).execute()
                execute.body()?.string()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }

        }
        return null
    }
}
