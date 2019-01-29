package com.fungo.socialgo.model.token

import android.content.Context
import android.content.SharedPreferences

import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.utils.SocialGoUtils

/**
 * 登录Token，基类
 */
abstract class AccessToken {

    var openid: String? = null        // 授权用户唯一标识。
    var unionid: String? = null
    var access_token: String? = null  // 接口调用凭证
    var expires_in: Long = 0          // access_token接口调用凭证超时时间，单位（秒）

    val isValid: Boolean
        get() = if (loginTarget() == Target.LOGIN_WX) {
            access_token != null && unionid != null
        } else
            access_token != null && openid != null

    val socialId: String?
        get() = if (loginTarget() == Target.LOGIN_WX) {
            unionid
        } else
            openid

    /**
     * 登录类型
     */
    abstract fun loginTarget(): Int

    override fun toString(): String {
        return "BaseAccessToken{" +
                "openid='" + openid + '\''.toString() +
                ", unionid='" + unionid + '\''.toString() +
                ", access_token='" + access_token + '\''.toString() +
                ", expires_in=" + expires_in +
                '}'.toString()
    }

    companion object {
        // 静态 token 存取
        const val TOKEN_STORE = "TOKEN_STORE"
        const val WECHAT_TOKEN_KEY = "WECHAT_TOKEN_KEY"
        const val SINA_TOKEN_KEY = "SINA_TOKEN_KEY"
        const val QQ_TOKEN_KEY = "QQ_TOKEN_KEY"

        private fun getSp(context: Context): SharedPreferences {
            return context.getSharedPreferences(TOKEN_STORE + SocialSdk.getConfig().getAppName(), Context.MODE_PRIVATE)
        }

        fun <T> getToken(context: Context?, key: String, tokenClazz: Class<T>): T? {
            if (context == null) {
                return null
            }
            val sp = getSp(context)
            return SocialGoUtils.getObject(sp.getString(key, null), tokenClazz)
        }

        fun saveToken(context: Context?, key: String, token: Any?) {
            if (context != null) {
                SocialSdk.getExecutor().execute {
                    try {
                        val sp = getSp(context)
                        val tokenJson = SocialGoUtils.getObject2Json(token)
                        sp.edit().putString(key, tokenJson).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // 清理平台 token
        fun clearToken(context: Context?, @Target.LoginTarget platform: Int) {
            if (context != null) {
                var key: String? = null
                when (platform) {
                    Target.LOGIN_QQ -> key = QQ_TOKEN_KEY
                    Target.LOGIN_WB -> key = SINA_TOKEN_KEY
                    Target.LOGIN_WX -> key = WECHAT_TOKEN_KEY
                }
                if (key != null) {
                    val edit = getSp(context).edit()
                    edit.remove(key).apply()
                }
            }

        }
    }
}
