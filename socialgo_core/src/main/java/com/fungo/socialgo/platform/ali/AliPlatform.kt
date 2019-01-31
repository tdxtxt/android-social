package com.fungo.socialgo.platform.ali

import android.app.Activity
import android.content.Context

import com.fungo.socialgo.SocialGo
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.platform.AbsPlatform
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.PlatformCreator
import com.fungo.socialgo.uikit.BaseSocialActivity

/**
 * 支付宝支付平台
 */
class AliPlatform(appId: String?, appName: String?) : AbsPlatform(appId, appName) {

    override fun getActionClazz(): Class<*> {
        return BaseSocialActivity::class.java
    }

    /**
     * 调起支付接口
     */
    override fun doPay(context: Context, params: String, listener: OnPayListener) {
        Alipay(context, params, listener).doPay()
    }

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            return AliPlatform("", SocialGo.getConfig().getAppName())
        }
    }

    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {

    }

    override fun recycle() {

    }
}
