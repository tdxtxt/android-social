package com.fungo.socialgo

import android.content.Context
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.platform.Target
import java.io.File


/**
 * 第三方平台信息配置
 */
class SocialSdkConfig private constructor() {

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
    private var disablePlatforms = arrayListOf<Int>()

    fun disablePlatform(@Target.PlatformTarget platform: Int): SocialSdkConfig {
        this.disablePlatforms.add(platform)
        return this
    }

    fun qq(qqAppId: String): SocialSdkConfig {
        this.qqAppId = qqAppId
        return this
    }

    fun wechat(wxAppId: String, wxSecretKey: String): SocialSdkConfig {
        this.wxSecretKey = wxSecretKey
        this.wxAppId = wxAppId
        return this
    }

    fun wechat(wxAppId: String, wxSecretKey: String, onlyAuthCode: Boolean): SocialSdkConfig {
        this.isOnlyAuthCode = onlyAuthCode
        this.wxSecretKey = wxSecretKey
        this.wxAppId = wxAppId
        return this
    }

    fun sina(sinaAppId: String): SocialSdkConfig {
        this.sinaAppId = sinaAppId
        return this
    }

    fun sinaScope(sinaScope: String): SocialSdkConfig {
        this.sinaScope = sinaScope
        return this
    }

    fun sinaRedirectUrl(sinaRedirectUrl: String): SocialSdkConfig {
        this.sinaRedirectUrl = sinaRedirectUrl
        return this
    }

    fun defImageResId(defImageResId: Int): SocialSdkConfig {
        this.defImageResId = defImageResId
        return this
    }

    fun appName(appName: String): SocialSdkConfig {
        this.appName = appName
        return this
    }

    fun debug(debug: Boolean): SocialSdkConfig {
        this.isDebug = debug
        return this
    }

    fun getDefImageResId(): Int {
        return defImageResId
    }

    fun getDisablePlatforms(): List<Int> {
        return disablePlatforms
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
        return "SocialSdkConfig{" +
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

        private const val SHARE_CACHE_DIR_NAME = "toShare"

        // 静态工厂
        fun create(context: Context): SocialSdkConfig {
            val config = SocialSdkConfig()
            val shareDir = File(context.externalCacheDir, SHARE_CACHE_DIR_NAME)
            config.cacheDir = (if (shareDir.mkdirs()) shareDir else context.cacheDir).absolutePath
            // init
            config.appName = "android_app"
            config.disablePlatforms.clear()
            config.sinaRedirectUrl = SocialConstants.REDIRECT_URL
            config.sinaScope = SocialConstants.SCOPE
            config.isDebug = false
            return config
        }
    }
}