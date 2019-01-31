package com.fungo.socialgo.qq

import android.app.Activity
import android.content.Context
import android.content.Intent

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.model.LoginResult
import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.qq.model.QQAccessToken
import com.fungo.socialgo.qq.model.QQUser
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import com.tencent.connect.UserInfo
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError

import org.json.JSONObject

import java.lang.ref.WeakReference

/**
 * QQ登录助手
 */
class QQLoginHelper(activity: Activity, private val tencentApi: Tencent, private val listener: OnLoginListener) {

    private val loginType: Int = Target.LOGIN_QQ
    private val mActivityRef: WeakReference<Activity> = WeakReference(activity)
    private var loginUiListener: LoginUiListener? = null

    fun handleResultData(data: Intent?) {
        Tencent.handleResultData(data, loginUiListener)
    }

    // 登录
    fun login() {
        val qqToken = getToken()
        if (qqToken != null) {
            tencentApi.setAccessToken(qqToken.access_token, qqToken.expires_in.toString() + "")
            tencentApi.openId = qqToken.openid
            if (tencentApi.isSessionValid) {
                getUserInfo(qqToken)
            } else {
                loginUiListener = LoginUiListener()
                tencentApi.login(mActivityRef.get(), "all", loginUiListener)
            }
        } else {
            loginUiListener = LoginUiListener()
            tencentApi.login(mActivityRef.get(), "all", loginUiListener)
        }
    }

    // 登录监听包装类
    private inner class LoginUiListener : IUiListener {
        override fun onComplete(o: Any) {
            val jsonResponse = o as JSONObject
            val qqToken = SocialGoUtils.getObject(jsonResponse.toString(), QQAccessToken::class.java)
            SocialLogUtils.e("获取到 qq token = ", qqToken)
            if (qqToken == null) {
                listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_PARSE_ERROR, "#LoginUiListener#qq token is null, data = $qqToken"))
                return
            }
            // 保存token
            AccessToken.saveToken(getContext(), AccessToken.QQ_TOKEN_KEY, qqToken)
            tencentApi.setAccessToken(qqToken.access_token, qqToken.expires_in.toString() + "")
            tencentApi.openId = qqToken.openid
            getUserInfo(qqToken)
        }

        override fun onError(e: UiError) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "#LoginUiListener#获取用户信息失败 " + parseUiError(e)))
        }

        override fun onCancel() {
            listener.getFunction().onCancel?.invoke()
        }
    }

    private fun getUserInfo(qqToken: QQAccessToken?) {
        val info = UserInfo(getContext(), tencentApi.qqToken)
        info.getUserInfo(object : IUiListener {
            override fun onComplete(any: Any) {
                val qqUserInfo = SocialGoUtils.getObject(any.toString(), QQUser::class.java)
                if (qqUserInfo == null) {
                    listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_PARSE_ERROR, "#getUserInfo#解析 qq user 错误, data = " + any.toString()))
                } else {
                    qqUserInfo.setOpenId(tencentApi.openId)
                    listener.getFunction().onLoginSuccess?.invoke(LoginResult(loginType, qqUserInfo, qqToken))
                }
            }

            override fun onError(e: UiError) {
                listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "#getUserInfo#qq获取用户信息失败  " + parseUiError(e)))
            }

            override fun onCancel() {
                listener.getFunction().onCancel?.invoke()
            }
        })
    }

    private fun getContext(): Context? {
        return mActivityRef.get()?.applicationContext
    }

    private fun getToken(): QQAccessToken? {
        return AccessToken.getToken(getContext(), AccessToken.QQ_TOKEN_KEY, QQAccessToken::class.java)
    }

    private fun parseUiError(e: UiError): String {
        return "code = " + e.errorCode + " ,msg = " + e.errorMessage + " ,detail=" + e.errorDetail
    }
}
