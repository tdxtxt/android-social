package com.pingerx.socialgo.core.adapter.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.pingerx.socialgo.core.adapter.IJsonAdapter
import java.text.DateFormat
import java.util.*

/**
 * 功能描述:
 * @author tangdexiang
 * @since 2020/6/2
 */
class DefaultGsonAdapter : IJsonAdapter {
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