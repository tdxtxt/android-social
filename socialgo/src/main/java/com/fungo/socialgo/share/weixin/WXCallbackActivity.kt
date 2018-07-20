package com.fungo.socialgo.share.weixin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.fungo.socialgo.share.SocialApi
import com.fungo.socialgo.share.config.PlatformConfig
import com.fungo.socialgo.share.config.PlatformType
import com.fungo.socialgo.utils.SocialUtils
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * @author Pinger
 * @since 18-7-20 下午4:03
 * 微信的回调类
 */

class WXCallbackActivity : Activity(), IWXAPIEventHandler {

    private var mWXHandler: WXHandler? = null
    private var mWXCircleHandler: WXHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mWXHandler = SocialApi.getSSOHandler(PlatformType.WEIXIN) as WXHandler
        this.mWXHandler!!.onCreate(this.applicationContext, PlatformConfig.getPlatformConfig(PlatformType.WEIXIN))

        this.mWXCircleHandler = SocialApi.getSSOHandler(PlatformType.WEIXIN_CIRCLE) as WXHandler
        this.mWXCircleHandler!!.onCreate(this.applicationContext, PlatformConfig.getPlatformConfig(PlatformType.WEIXIN_CIRCLE))

        this.mWXHandler?.getWxApi()?.handleIntent(this.intent, this)
    }


    override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        this.mWXHandler = SocialApi.getSSOHandler(PlatformType.WEIXIN) as WXHandler
        this.mWXHandler!!.onCreate(this.applicationContext, PlatformConfig.getPlatformConfig(PlatformType.WEIXIN))

        this.mWXCircleHandler = SocialApi.getSSOHandler(PlatformType.WEIXIN_CIRCLE) as WXHandler
        this.mWXCircleHandler!!.onCreate(this.applicationContext, PlatformConfig.getPlatformConfig(PlatformType.WEIXIN_CIRCLE))

        this.mWXHandler?.getWxApi()?.handleIntent(this.intent, this)
    }

    override fun onResp(resp: BaseResp?) {
        try {
            this.mWXHandler?.getWXEventHandler()?.onResp(resp)
            this.mWXCircleHandler?.getWXEventHandler()?.onResp(resp)
        } catch (e: Exception) {
            SocialUtils.e(e.message)
        }

        this.finish()
    }

    override fun onReq(req: BaseReq?) {
        this.mWXHandler?.getWXEventHandler()?.onReq(req)
        this.finish()
    }
}