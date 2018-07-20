package com.fungo.socialgo.share.weibo

import android.app.Activity
import android.content.Context
import com.fungo.socialgo.share.config.PlatformConfig
import com.fungo.socialgo.share.config.PlatformType
import com.fungo.socialgo.share.config.SSOHandler
import com.fungo.socialgo.share.listener.OnAuthListener
import com.fungo.socialgo.share.listener.OnShareListener
import com.fungo.socialgo.share.media.IShareMedia
import com.fungo.socialgo.share.media.ShareTextImageMedia
import com.fungo.socialgo.utils.SocialUtils
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import com.sina.weibo.sdk.auth.sso.SsoHandler
import com.sina.weibo.sdk.share.WbShareHandler
import java.util.HashMap

/**
 * @author Pinger
 * @since 18-7-20 下午3:57
 * 新浪微博回调处理
 */

class SinaWBHandler: SSOHandler() {


    private var mContext: Context? = null
    private var mActivity: Activity? = null

    private var mSsoHandler: SsoHandler? = null

    private var mConfig: PlatformConfig.SinaWB? = null
    private var mAuthListener: OnAuthListener? = null
    private var mShareListener: OnShareListener? = null
    private var mWbShareHandler: WbShareHandler? = null

    private var REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"// 应用的回调页 要和微博开放平台的回调地址一致
    private val SCOPE = ""


    /**
     * 设置微博 REDIRECT_URL
     */
    fun setRedirctUrl(redirctUrl: String) {
        REDIRECT_URL = redirctUrl
    }


    override fun onCreate(context: Context, config: PlatformConfig.Platform) {
        this.mContext = context
        this.mActivity = context as Activity
        this.mConfig = config as PlatformConfig.SinaWB
        WbSdk.install(context, AuthInfo(context, mConfig!!.appKey, REDIRECT_URL, SCOPE))
        mWbShareHandler = WbShareHandler(context)
        mWbShareHandler!!.registerApp()
    }


    override fun authorize(activity: Activity, authListener: OnAuthListener) {
        this.mActivity = activity
        this.mAuthListener = authListener

        this.mSsoHandler = SsoHandler(activity)

        val mediaType = mConfig?.name?:PlatformType.SINA_WB
        mSsoHandler?.authorize(object : WbAuthListener {

            override fun onSuccess(accessToken: Oauth2AccessToken) {
                // 从 Bundle 中解析 Token
                if (accessToken.isSessionValid) {
                    val map = HashMap<String, String>()
                    map["uid"] = accessToken.uid
                    map["access_token"] = accessToken.token
                    map["refresh_token"] = accessToken.refreshToken
                    map["expire_time"] = "" + accessToken.expiresTime

                    mAuthListener?.onComplete(mediaType, map)
                } else {
                    val errmsg = "errmsg=accessToken is not SessionValid"
                    SocialUtils.e(errmsg)
                    mAuthListener?.onError(mediaType, errmsg)
                }
            }

            override fun cancel() {
                mAuthListener?.onCancel(mediaType)
            }

            override fun onFailure(wbConnectErrorMessage: WbConnectErrorMessage) {
                val errmsg = "errmsg=" + wbConnectErrorMessage.errorMessage
                SocialUtils.e(errmsg)
                mAuthListener?.onError(mediaType, errmsg)
            }
        })
    }


    override fun share(activity: Activity, shareMedia: IShareMedia, shareListener: OnShareListener) {
        this.mActivity = activity
        this.mShareListener = shareListener

        this.mSsoHandler = SsoHandler(activity)

        val weiboMessage = WeiboMultiMessage()

        if (shareMedia is ShareTextImageMedia) {       // 文字图片分享
            val (image, text) = shareMedia

            if (text.isNotEmpty()) {
                val textObject = TextObject()
                textObject.text = text
                weiboMessage.textObject = textObject
            }

            val imageObject = ImageObject()
            imageObject.setImageObject(image)
            weiboMessage.imageObject = imageObject
        } else {
                this.mShareListener?.onError(this.mConfig?.name?:PlatformType.SINA_WB, "weibo is not support this shareMedia")
            return
        }
        mWbShareHandler?.shareMessage(weiboMessage, false)
    }


}