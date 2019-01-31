package com.fungo.socialgo

import android.content.Context
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.utils.SocialGoUtils
import java.io.File


/**
 * 第三方平台信息配置
 */
class SocialGoConfig private constructor() {

    private var isDebug: Boolean = false            // 调试配置
    private var appName: String? = null             // 应用名
    private var wxAppId: String? = null             // 微信配置
    private var wxSecretKey: String? = null
    private var isOnlyAuthCode: Boolean = false

    private var qqAppId: String? = null           // qq 配置
    private var sinaAppId: String? = null         // 微博配置
    private var sinaRedirectUrl: String? = null
    private var sinaScope: String? = null

    private var cacheDir: String? = null         // 存储路径，不允许更改
    private var defImageResId: Int = 0           // 图片默认资源

    fun qq(qqAppId: String): SocialGoConfig {
        this.qqAppId = qqAppId
        return this
    }

    fun wechat(wxAppId: String, wxSecretKey: String): SocialGoConfig {
        this.wxSecretKey = wxSecretKey
        this.wxAppId = wxAppId
        return this
    }

    fun wechat(wxAppId: String, wxSecretKey: String, onlyAuthCode: Boolean): SocialGoConfig {
        this.isOnlyAuthCode = onlyAuthCode
        this.wxSecretKey = wxSecretKey
        this.wxAppId = wxAppId
        return this
    }

    fun sina(sinaAppId: String): SocialGoConfig {
        this.sinaAppId = sinaAppId
        return this
    }

    fun sinaScope(sinaScope: String): SocialGoConfig {
        this.sinaScope = sinaScope
        return this
    }

    fun sinaRedirectUrl(sinaRedirectUrl: String): SocialGoConfig {
        this.sinaRedirectUrl = sinaRedirectUrl
        return this
    }

    fun defImageResId(defImageResId: Int): SocialGoConfig {
        this.defImageResId = defImageResId
        return this
    }

    fun appName(appName: String): SocialGoConfig {
        this.appName = appName
        return this
    }

    fun debug(debug: Boolean): SocialGoConfig {
        this.isDebug = debug
        return this
    }

    fun getDefImageResId(): Int {
        return defImageResId
    }

    fun getCacheDir(): String? {
        return cacheDir
    }

    fun getAppName(): String? {
        return appName
    }

    fun getWxAppId(): String? {
        return wxAppId
    }

    fun getWxSecretKey(): String? {
        return wxSecretKey
    }

    fun getQqAppId(): String? {
        return qqAppId
    }

    fun getSinaAppId(): String? {
        return sinaAppId
    }

    fun getSinaRedirectUrl(): String? {
        return sinaRedirectUrl
    }

    fun getSinaScope(): String? {
        return sinaScope
    }

    fun isDebug(): Boolean {
        return this.isDebug
    }

    fun isOnlyAuthCode(): Boolean {
        return this.isOnlyAuthCode
    }

    override fun toString(): String {
        return "SocialGoConfig{" +
                "appName='" + appName + '\''.toString() +
                ", wxAppId='" + wxAppId + '\''.toString() +
                ", wxSecretKey='" + wxSecretKey + '\''.toString() +
                ", qqAppId='" + qqAppId + '\''.toString() +
                ", sinaAppId='" + sinaAppId + '\''.toString() +
                ", sinaRedirectUrl='" + sinaRedirectUrl + '\''.toString() +
                ", sinaScope='" + sinaScope + '\''.toString() +
                ", cacheDir='" + cacheDir + '\''.toString() +
                '}'.toString()
    }

    companion object {

        private const val CACHE_DIR_NAME = "socialgo"

        // 静态工厂
        fun create(context: Context): SocialGoConfig {
            val config = SocialGoConfig()
            val shareDir = File(context.externalCacheDir, CACHE_DIR_NAME)
            if (!SocialGoUtils.isExist(shareDir)) {
                shareDir.mkdirs()
            }
            config.cacheDir = shareDir.absolutePath
            // init
            config.appName = "android_app"
            config.sinaRedirectUrl = SocialConstants.REDIRECT_URL
            config.sinaScope = SocialConstants.SCOPE
            config.isDebug = false
            return config
        }
    }
}