package com.pingerx.socialgo.weibo

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.pingerx.socialgo.core.SocialGo
import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.listener.OnLoginListener
import com.pingerx.socialgo.core.listener.OnShareListener
import com.pingerx.socialgo.core.model.ShareEntity
import com.pingerx.socialgo.core.platform.AbsPlatform
import com.pingerx.socialgo.core.platform.IPlatform
import com.pingerx.socialgo.core.platform.PlatformCreator
import com.pingerx.socialgo.core.platform.Target
import com.pingerx.socialgo.core.utils.SocialGoUtils
import com.pingerx.socialgo.weibo.uikit.WbActionActivity
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.constant.WBConstants
import com.sina.weibo.sdk.share.WbShareCallback

/**
 * 新浪微博平台实现
 * 文本相同的分享不允许重复发送，会发送不出去
 * 分享支持的检测
 */
class WbPlatform constructor(context: Context, appId: String?, appName: String?, redirectUrl: String?, scope: String?) : AbsPlatform(appId, appName) {

    private var mLoginHelper: WbLoginHelper? = null
    private var mShareHelper: WbShareHelper? = null

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            val appId = config.getWeiboAppId()
            val appName = config.getAppName()
            val redirectUrl = config.getWeiboRedirectUrl()
            val scope = config.getWeiboScope()
            if (SocialGoUtils.isAnyEmpty(appId, appName, redirectUrl, scope)) {
                throw IllegalArgumentException(SocialError.MSG_WB_ID_NULL)
            }
            val platform = WbPlatform(context, appId, appName, redirectUrl, scope)
            platform.setTarget(target)
            return platform
        }
    }

    init {
        WbSdk.install(context, AuthInfo(context, appId, redirectUrl, scope))
    }

    override fun isInstall(context: Context): Boolean {
        // 支持网页授权，所以不需要安装 app
        return if (mTarget == Target.LOGIN_WB) {
            true
        } else WbSdk.isWbInstall(context)
    }

    override fun recycle() {
        mShareHelper?.recycle()
        mShareHelper = null
        mLoginHelper?.recycle()
        mLoginHelper = null
    }

    override fun getActionClazz(): Class<*> {
        return WbActionActivity::class.java
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mLoginHelper?.authorizeCallBack(requestCode, resultCode, data)
    }

    override fun handleIntent(activity: Activity) {
        if (activity is WbShareCallback) {
            mShareHelper?.doResultIntent(activity.intent, activity as WbShareCallback)
        }
    }

    override fun onResponse(resp: Any) {
        if (resp is Int) {
            when (resp) {
                WBConstants.ErrorCode.ERR_OK ->     // 分享成功
                    mShareHelper?.onSuccess()
                WBConstants.ErrorCode.ERR_CANCEL -> // 分享取消
                    mShareHelper?.onCancel()
                WBConstants.ErrorCode.ERR_FAIL ->   // 分享失败
                    mShareHelper?.onFailure(SocialError(SocialError.CODE_SDK_ERROR, "$#微博分享失败"))
            }
        }
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        if (mLoginHelper == null) {
            mLoginHelper = WbLoginHelper(activity)
        }
        mLoginHelper!!.login(activity, listener)
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        if (mShareHelper == null) {
            mShareHelper = WbShareHelper()
        }
        mShareHelper!!.share(activity, target, entity, listener)
    }
}
