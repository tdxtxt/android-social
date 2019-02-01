package com.pingerx.socialgo.wechat

import android.content.Context

import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.listener.OnLoginListener
import com.pingerx.socialgo.core.model.LoginResult
import com.pingerx.socialgo.core.model.token.AccessToken
import com.pingerx.socialgo.core.platform.Target
import com.pingerx.socialgo.wechat.model.WeChatAccessToken
import com.pingerx.socialgo.wechat.model.WxUser
import com.pingerx.socialgo.core.utils.SocialGoUtils
import com.pingerx.socialgo.core.utils.SocialLogUtils
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI

import java.lang.ref.WeakReference

/**
 * 微信登录助手
 */
class WxLoginHelper(context: Context, private val wxapi: IWXAPI?, private val appId: String?) {

    private val mLoginType: Int = Target.LOGIN_WX
    private val mContextRef: WeakReference<Context> = WeakReference(context)
    private var mSecretKey: String? = null
    private var mListener: OnLoginListener? = null

    /**
     * 开始登录
     */
    fun login(secretKey: String?, listener: OnLoginListener) {
        this.mListener = listener
        this.mSecretKey = secretKey
        // 检测本地token的机制
        val storeToken = getToken()
        if (storeToken != null && storeToken.isValid) {
            checkAccessTokenValid(storeToken)
        } else {
            // 本地没有token, 发起请求，wxEntry将会获得code，接着获取access_token
            sendAuthReq()
        }
    }

    /**
     * 发起申请
     */
    private fun sendAuthReq() {
        SocialLogUtils.e("本地没有token,发起登录")
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "carjob_wx_login"
        wxapi?.sendReq(req)
    }

    /**
     * 刷新token,当access_token失效时使用,使用refresh_token获取新的token
     *
     * @param token 用来放 refresh_token
     */
    private fun refreshToken(token: WeChatAccessToken) {
        SocialLogUtils.e("token失效，开始刷新token")
        SocialGoUtils.startJsonRequest(buildRefreshTokenUrl(token), WeChatAccessToken::class.java, object : SocialGoUtils.Callback<WeChatAccessToken> {
            override fun onSuccess(data: WeChatAccessToken?) {
                // 获取到access_token
                if (data?.isNoError == true) {
                    SocialLogUtils.e("刷新token成功 token = $data")
                    AccessToken.saveToken(mContextRef.get(), AccessToken.WECHAT_TOKEN_KEY, data)
                    // 刷新完成，获取用户信息
                    getUserInfoByValidToken(data)
                } else {
                    SocialLogUtils.e("code = " + data?.errcode + "  ,msg = " + data?.errmsg)
                    sendAuthReq()
                }
            }

            override fun onFailure(e: SocialError) {
                // 刷新token失败
                mListener?.getFunction()?.onFailure?.invoke(e.append("refreshToken fail"))
            }
        })
    }

    /**
     * 根据code获取access_token
     *
     * @param code code
     */
    fun getAccessTokenByCode(code: String) {
        SocialLogUtils.e("使用code获取access_token $code")
        SocialGoUtils.startJsonRequest(buildGetTokenUrl(code), WeChatAccessToken::class.java, object : SocialGoUtils.Callback<WeChatAccessToken> {
            override fun onSuccess(data: WeChatAccessToken?) {
                // 获取到access_token
                if (data?.isNoError == true) {
                    AccessToken.saveToken(mContextRef.get(), AccessToken.WECHAT_TOKEN_KEY, data)
                    getUserInfoByValidToken(data)
                } else {
                    val exception = SocialError(SocialError.CODE_REQUEST_ERROR, "#getAccessTokenByCode#获取access_token失败 code = " + data?.errcode + "  msg = " + data?.errmsg)
                    mListener?.getFunction()?.onFailure?.invoke(exception)
                }
            }

            override fun onFailure(e: SocialError) {
                // 获取access_token失败
                mListener?.getFunction()?.onFailure?.invoke(e.append("getAccessTokenByCode fail"))
            }
        })
    }

    fun getLoginListener(): OnLoginListener? {
        return mListener
    }

    /**
     * 检测token有效性
     *
     * @param token 用来拿access_token
     */
    private fun checkAccessTokenValid(token: WeChatAccessToken) {
        SocialLogUtils.e("本地存了token,开始检测有效性" + token.toString())
        SocialGoUtils.startJsonRequest(buildCheckAccessTokenValidUrl(token), TokenValidResp::class.java, object : SocialGoUtils.Callback<TokenValidResp> {
            override fun onSuccess(data: TokenValidResp?) {
                // 检测是否有效
                SocialLogUtils.e("检测token结束，结果 = " + data.toString())
                if (data?.isNoError == true) {
                    // access_token有效。开始获取用户信息
                    getUserInfoByValidToken(token)
                } else {
                    // access_token失效，刷新或者获取新的
                    refreshToken(token)
                }
            }

            override fun onFailure(e: SocialError) {
                // 检测access_token有效性失败
                SocialLogUtils.e("检测access_token失败")
                mListener?.getFunction()?.onFailure?.invoke(e.append("checkAccessTokenValid fail"))
            }
        })
    }

    /**
     * token是ok的，获取用户信息
     *
     * @param token 用来拿access_token
     */
    private fun getUserInfoByValidToken(token: WeChatAccessToken) {
        SocialLogUtils.e("access_token有效，开始获取用户信息")
        SocialGoUtils.startJsonRequest(buildFetchUserInfoUrl(token), WxUser::class.java, object : SocialGoUtils.Callback<WxUser> {
            override fun onSuccess(data: WxUser?) {
                SocialLogUtils.e("获取到用户信息" + data.toString())
                if (data?.isNoError == true) {
                    mListener?.getFunction()?.onLoginSuccess?.invoke(LoginResult(mLoginType, data, token))
                } else {
                    mListener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_REQUEST_ERROR, "#getUserInfoByValidToken#login code = " + data?.errcode + " ,msg = " + data?.errmsg))
                }
            }

            override fun onFailure(e: SocialError) {
                // 获取用户信息失败
                mListener?.getFunction()?.onFailure?.invoke(e.append("getUserInfoByValidToken fail"))
            }
        })
    }

    private fun buildRefreshTokenUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/oauth2/refresh_token"
                + "?appid=" + appId
                + "&grant_type=" + "refresh_token"
                + "&refresh_token=" + token.access_token)
    }

    private fun buildGetTokenUrl(code: String): String {
        return (BASE_URL
                + "/oauth2/access_token"
                + "?appid=" + appId
                + "&secret=" + mSecretKey
                + "&code=" + code
                + "&grant_type=" + "authorization_code")
    }

    private fun buildCheckAccessTokenValidUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/auth"
                + "?access_token=" + token.access_token
                + "&openid=" + token.openid)
    }

    private fun buildFetchUserInfoUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/userinfo"
                + "?access_token=" + token.access_token
                + "&openid=" + token.openid)
    }

    private fun getToken(): WeChatAccessToken? {
        return AccessToken.getToken(mContextRef.get(), AccessToken.WECHAT_TOKEN_KEY, WeChatAccessToken::class.java)
    }


    /**
     * 检测token有效性的resp
     */
    private class TokenValidResp {
        var errcode: Int = 0
        var errmsg: String? = null
        val isNoError: Boolean
            get() = errcode == 0

        override fun toString(): String {
            return "TokenValidResp{" +
                    "errcode=" + errcode +
                    ", errmsg='" + errmsg + '\''.toString() +
                    '}'.toString()
        }
    }

    companion object {
        private const val BASE_URL = "https://api.weixin.qq.com/sns"
    }

}
