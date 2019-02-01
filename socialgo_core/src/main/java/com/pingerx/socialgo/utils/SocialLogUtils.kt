package com.pingerx.socialgo.utils

import android.util.Log

import com.pingerx.socialgo.SocialGo

import org.json.JSONArray
import org.json.JSONObject

/**
 * 内部工具类
 */
object SocialLogUtils {

    private const val TAG = "SocialGo"

    private fun getMsg(msg: Any?): String {
        return msg?.toString() ?: "null"
    }

    fun e(msg: Any?) {
        if (SocialGo.getConfig().isDebug())
            Log.e("$TAG|", getMsg(msg))
    }

    fun e(vararg msg: Any?) {
        if (SocialGo.getConfig().isDebug()) {
            val sb = StringBuilder()
            for (o in msg) {
                sb.append(" ").append(getMsg(o)).append(" ")
            }
            Log.e("$TAG|", sb.toString())
        }
    }


    fun t(throwable: Throwable?) {
        if (SocialGo.getConfig().isDebug()) {
            Log.e(TAG, throwable?.message, throwable)
        }
    }


    fun json(json: String?) {
        if (SocialGo.getConfig().isDebug()) {
            val sb = StringBuilder()
            if (json == null || json.trim { it <= ' ' }.isEmpty()) {
                sb.append("json isEmpty => ").append(json)
            } else {
                try {
                    when {
                        json.startsWith("{") -> {
                            val jsonObject = JSONObject(json)
                            sb.append(jsonObject.toString(2))
                        }
                        json.startsWith("[") -> {
                            val jsonArray = JSONArray(json)
                            sb.append(jsonArray.toString(2))
                        }
                        else -> sb.append("json 格式错误 => ").append(json)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    sb.append("json formatError => ").append(json)
                }
            }
            e(sb.toString())
        }
    }
}
