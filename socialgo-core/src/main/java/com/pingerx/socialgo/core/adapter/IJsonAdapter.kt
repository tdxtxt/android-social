package com.pingerx.socialgo.core.adapter


/**
 * Json数据解析适配器
 * 用于解析返回回来的数据类型
 */
interface IJsonAdapter {

    /**
     * 将Json解析成数据实体
     */
    fun <T> fromJson(jsonString: String, cls: Class<T>): T?

    /**
     * 将对象生成json
     */
    fun toJson(any: Any?): String?
}
