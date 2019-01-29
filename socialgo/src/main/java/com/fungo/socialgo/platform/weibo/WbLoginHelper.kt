package com.fungo.socialgo.platform.weibo

import android.app.Activity
import android.content.Intent

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.Recyclable
import com.fungo.socialgo.model.LoginResult
import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.platform.weibo.model.SinaAccessToken
import com.fungo.socialgo.platform.weibo.model.SinaUser
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import com.sina.weibo.sdk.auth.sso.SsoHandler


/**
 * 微博登陆辅助工具类
 */
class WbLoginHelper(context: Activity) : Recyclable {

    private val mLoginType: Int = Target.LOGIN_WB
    private var mOnLoginListener: OnLoginListener? = null
    private var mSsoHandler: SsoHandler = SsoHandler(context)

    /**
     * 获取用户信息
     *
     * @param token token
     */
    private fun getUserInfo(token: Oauth2AccessToken?) {
        SocialGoUtils.startJsonRequest("https://api.weibo.com/2/users/show.json?access_token=" + token?.token + "&uid=" + token?.uid, SinaUser::class.java, object : SocialGoUtils.Callback<SinaUser> {
            override fun onSuccess(data: SinaUser?) {
                SocialLogUtils.e(SocialGoUtils.getObject2Json(data))
                if (data != null && token != null) {
                    mOnLoginListener?.onSuccess(LoginResult(mLoginType, data, SinaAccessToken(token)))
                } else {
                    onFailure(SocialError(SocialError.CODE_DATA_EMPTY))
                }
            }

            override fun onFailure(e: SocialError) {
                mOnLoginListener?.onFailure(e)
            }
        })
    }

    fun login(activity: Activity, loginListener: OnLoginListener?) {
        if (loginListener == null)
            return
        this.mOnLoginListener = loginListener
        justAuth(activity, object : WbAuthListener {
            override fun onSuccess(oauth2AccessToken: Oauth2AccessToken?) {
                getUserInfo(oauth2AccessToken)
            }

            override fun cancel() {
                loginListener.onCancel()
            }

            override fun onFailure(msg: WbConnectErrorMessage) {
                loginListener.onFailure(SocialError(SocialError.CODE_SDK_ERROR, "#login#connect error," + msg.errorCode + " " + msg.errorMessage))
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
