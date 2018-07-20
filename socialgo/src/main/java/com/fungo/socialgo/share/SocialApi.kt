package com.fungo.socialgo.share

import android.app.Activity
import android.content.Intent
import com.fungo.socialgo.share.config.PlatformConfig
import com.fungo.socialgo.share.config.PlatformType
import com.fungo.socialgo.share.config.SSOHandler
import com.fungo.socialgo.share.listener.OnAuthListener
import com.fungo.socialgo.share.listener.OnShareListener
import com.fungo.socialgo.share.media.IShareMedia
import com.fungo.socialgo.share.qq.QQHandler
import com.fungo.socialgo.share.weibo.SinaWBHandler
import com.fungo.socialgo.share.weixin.WXHandler
import java.util.*

/**
 * @author Pinger
 * @since 18-7-20 下午4:14
 * 分享API
 */

object SocialApi {

    private val mMapSSOHandler = HashMap<PlatformType, SSOHandler>()

    /**
     * 第三方登录授权
     * @param activity
     * @param platformType 第三方平台
     * @param authListener 授权回调
     */
    fun doOauthVerify(activity: Activity, platformType: PlatformType, authListener: OnAuthListener) {
        val ssoHandler = getSSOHandler(platformType)
        ssoHandler?.onCreate(activity, PlatformConfig.getPlatformConfig(platformType))
        ssoHandler?.authorize(activity, authListener)
    }

    /**
     * 分享
     * @param platformType
     * @param shareMedia
     * @param shareListener
     */
    fun doShare(activity: Activity, platformType: PlatformType, shareMedia: IShareMedia, shareListener: OnShareListener) {
        val ssoHandler = getSSOHandler(platformType)
        ssoHandler?.onCreate(activity, PlatformConfig.getPlatformConfig(platformType))
        ssoHandler?.share(activity, shareMedia, shareListener)
    }

    /**
     * actvitiy result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        for ((_, value) in mMapSSOHandler) {
            value.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getSSOHandler(platformType: PlatformType): SSOHandler? {
        if (mMapSSOHandler[platformType] == null) {
            when (platformType) {
                PlatformType.WEIXIN -> mMapSSOHandler[platformType] = WXHandler()

                PlatformType.WEIXIN_CIRCLE -> mMapSSOHandler[platformType] = WXHandler()

                PlatformType.QQ -> mMapSSOHandler[platformType] = QQHandler()

                PlatformType.QZONE -> mMapSSOHandler[platformType] = QQHandler()

                PlatformType.SINA_WB -> mMapSSOHandler[platformType] = SinaWBHandler()
            }
        }

        return mMapSSOHandler[platformType]
    }

}