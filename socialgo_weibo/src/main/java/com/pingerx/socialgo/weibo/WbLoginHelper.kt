package com.pingerx.socialgo.weibo

import android.app.Activity
import android.content.Intent
import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.listener.OnLoginListener
import com.pingerx.socialgo.core.listener.Recyclable
import com.pingerx.socialgo.core.model.LoginResult
import com.pingerx.socialgo.core.model.token.AccessToken
import com.pingerx.socialgo.core.platform.Target
import com.pingerx.socialgo.core.utils.SocialGoUtils
import com.pingerx.socialgo.core.utils.SocialLogUtils
import com.pingerx.socialgo.weibo.model.SinaAccessToken
import com.pingerx.socialgo.weibo.model.SinaUser
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import com.sina.weibo.sdk.auth.sso.SsoHandler


/**
 * 微博登陆辅助工具类
 */
class WbLoginHelper(context: Activity) : Recyclable {

    private val mLoginType: Int = Target.LOGIN_WB
    private var onLoginListener: OnLoginListener? = null
    private var mSsoHandler: SsoHandler = SsoHandler(context)

    /**
     * 获取用户信息
     */
    private fun getUserInfo(token: Oauth2AccessToken?) {
        SocialGoUtils.startJsonRequest("https://api.weibo.com/2/users/show.json?access_token=" + token?.token + "&uid=" + token?.uid, SinaUser::class.java, object : SocialGoUtils.Callback<SinaUser> {
            override fun onSuccess(data: SinaUser?) {
                SocialLogUtils.e(SocialGoUtils.getObject2Json(data))
                if (data != null && token != null) {
                    onLoginListener?.getFunction()?.onLoginSuccess?.invoke(LoginResult(mLoginType, data, SinaAccessToken(token)))
                } else {
                    onFailure(SocialError(SocialError.CODE_DATA_EMPTY))
                }
            }

            override fun onFailure(e: SocialError) {
                onLoginListener?.getFunction()?.onFailure?.invoke(e)
            }
        })
    }

    fun login(activity: Activity, listener: OnLoginListener) {
        onLoginListener = listener
        justAuth(activity, object : WbAuthListener {
            override fun onSuccess(oauth2AccessToken: Oauth2AccessToken?) {
                getUserInfo(oauth2AccessToken)
            }

            override fun cancel() {
                listener.getFunction().onCancel?.invoke()
            }

            override fun onFailure(msg: WbConnectErrorMessage) {
                listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "#login#connect error," + msg.errorCode + " " + msg.errorMessage))
            }
        })
    }

    fun justAuth(activity: Activity, listener: WbAuthListener) {
        val token = AccessToken.getToken(activity, AccessToken.SINA_TOKEN_KEY, Oauth2AccessToken::class.java)
        if (token != null && token.isSessionValid && token.expiresTime > System.currentTimeMillis()) {
            listener.onSuccess(token)
        } else {
            AccessToken.clearToken(activity, Target.LOGIN_WB)
            mSsoHandler.authorize(object : WbAuthListener {
                override fun onSuccess(oauth2AccessToken: Oauth2AccessToken?) {
                    oauth2AccessToken?.bundle = null

                    SocialLogUtils.json(oauth2AccessToken?.toString())
                    AccessToken.saveToken(activity, AccessToken.SINA_TOKEN_KEY, oauth2AccessToken)
                    listener.onSuccess(oauth2AccessToken)
                }

                override fun cancel() {
                    listener.cancel()
                }

                override fun onFailure(wbConnectErrorMessage: WbConnectErrorMessage) {
                    listener.onFailure(wbConnectErrorMessage)
                }
            })
        }
    }

    fun authorizeCallBack(requestCode: Int, resultCode: Int, data: Intent?) {
        mSsoHandler.authorizeCallBack(requestCode, resultCode, data)
    }


    override fun recycle() {
    }
}
