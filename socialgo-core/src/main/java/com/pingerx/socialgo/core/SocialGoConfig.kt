package com.pingerx.socialgo.core

import android.content.Context
import com.pingerx.socialgo.core.common.SocialConstants
import com.pingerx.socialgo.core.utils.SocialGoUtils
import java.io.File


/**
 * 第三方平台信息配置
 */
class SocialGoConfig private constructor() {

    private var isDebug: Boolean = false            // 调试配置
    private var appName: String? = null             // 应用名
    private var wxAppId: String? = null             // 微信配置
    private var startAction: String = "com.social.startaction" //微信h5拉起App发送广播Action
    private var wxSecretKey: String? = null
    private var isOnlyAuthCode: Boolean = false

    private var qqAppId: String? = null           // qq 配置
    private var weiboAppId: String? = null         // 微博配置
    private var weiboRedirectUrl: String? = null
    private var weiboScope: String? = null

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

    fun weibo(weiboAppId: String): SocialGoConfig {
        this.weiboAppId = weiboAppId
        return this
    }

    fun weiboScope(weiboScope: String): SocialGoConfig {
        this.weiboScope = weiboScope
        return this
    }

    fun weiboRedirectUrl(weiboRedirectUrl: String): SocialGoConfig {
        this.weiboRedirectUrl = weiboRedirectUrl
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

    fun startAction(startAction: String): SocialGoConfig {
        this.startAction = startAction
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

    fun getStartAction(): String? {
        return startAction
    }

    fun getWxSecretKey(): String? {
        return wxSecretKey
    }

    fun getQqAppId(): String? {
        return qqAppId
    }

    fun getWeiboAppId(): String? {
        return weiboAppId
    }

    fun getWeiboRedirectUrl(): String? {
        return weiboRedirectUrl
    }

    fun getWeiboScope(): String? {
        return weiboScope
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
                ", sinaAppId='" + weiboAppId + '\''.toString() +
                ", sinaRedirectUrl='" + weiboRedirectUrl + '\''.toString() +
                ", sinaScope='" + weiboScope + '\''.toString() +
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
            config.weiboRedirectUrl = SocialConstants.REDIRECT_URL
            config.weiboScope = SocialConstants.SCOPE
            config.isDebug = false
            return config
        }
    }
}