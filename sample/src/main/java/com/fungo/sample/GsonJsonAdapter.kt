package com.fungo.sample

import com.fungo.socialgo.adapter.IJsonAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.text.DateFormat
import java.util.*

/**
 * CreateAt : 2017/11/25
 * Describe : 默认使用 gson 转换
 *
 * @author chendong
 */
class GsonJsonAdapter : IJsonAdapter {

    override fun <T> fromJson(jsonString: String, cls: Class<T>): T? {
        var t: T? = null
        try {
            val gson = Gson()
            t = gson.fromJson(jsonString, cls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return t
    }

    override fun toJson(any: Any?): String? {
        val gsonBuilder = GsonBuilder()
                .registerTypeAdapter(Date::class.java, JsonSerializer<Date> { src, _, _ -> JsonPrimitive(src.time) }).setDateFormat(DateFormat.LONG)
        return gsonBuilder.create().toJson(any)
    }
}
