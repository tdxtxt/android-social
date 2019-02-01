package com.pingerx.socialgo.core.adapter

import java.io.File

/**
 * 请求适配器，抽离底层的接口请求
 */
interface IRequestAdapter {

    /**
     * GET请求，获取一个文件
     */
    fun getFile(url: String): File?

    /**
     * GET请求，获取Json数据
     */
    fun getJson(url: String): String?

    /**
     * POST请求数据
     */
    fun postData(url: String, params: Map<String, String>, fileKey: String, filePath: String): String?
}
