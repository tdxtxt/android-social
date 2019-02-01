package com.pingerx.socialgo.qq

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
import com.pingerx.socialgo.qq.uikit.QQActionActivity
import com.pingerx.socialgo.core.utils.SocialGoUtils
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent

/**
 * 问题汇总：com.mTencentApi.tauth.AuthActivity需要添加（ <data android:scheme="tencent110557146"></data>）否则会一直返回分享取消
 * qq空间支持本地视频分享，网络视频使用web形式分享
 * qq好友不支持本地视频分享，支持网络视频分享
 *
 *
 * 登录分享文档 http://wiki.open.qq.com/wiki/QQ%E7%94%A8%E6%88%B7%E8%83%BD%E5%8A%9B
 */
class QQPlatform constructor(context: Context, appId: String?, appName: String?) : AbsPlatform(appId, appName) {

    private var mTencentApi: Tencent = Tencent.createInstance(appId, context)
    private var mQQLoginHelper: QQLoginHelper? = null
    private var mQQShareHelper: QQShareHelper? = null

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            if (SocialGoUtils.isAnyEmpty(config.getQqAppId(), config.getAppName())) {
                throw IllegalArgumentException(SocialError.MSG_QQ_ID_NULL)
            }
            return QQPlatform(context, config.getQqAppId(), config.getAppName())
        }
    }

    override fun getActionClazz(): Class<*> {
        return QQActionActivity::class.java
    }

    override fun recycle() {
        mTencentApi.releaseResource()
    }

    override fun isInstall(context: Context): Boolean {
        return mTencentApi.isQQInstalled(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
            Tencent.handleResultData(data, mQQShareHelper?.getUIListener())
        } else if (requestCode == Constants.REQUEST_LOGIN) {
            mQQLoginHelper?.handleResultData(data)
        }
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        if (!mTencentApi.isSupportSSOLogin(activity)) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_VERSION_LOW))
            return
        }
        if (mQQLoginHelper == null) {
            mQQLoginHelper = QQLoginHelper(activity, mTencentApi, listener)
        }
        mQQLoginHelper!!.login()
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        if (mQQShareHelper == null) {
            mQQShareHelper = QQShareHelper(mTencentApi, appName)
        }
        mQQShareHelper!!.share(activity, target, entity, listener)
    }
}
