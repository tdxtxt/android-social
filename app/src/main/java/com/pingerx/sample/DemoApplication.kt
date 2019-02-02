package com.pingerx.sample

import android.app.Application
import com.pingerx.socialgo.alipay.AliPlatform
import com.pingerx.socialgo.core.SocialGo
import com.pingerx.socialgo.core.SocialGoConfig
import com.pingerx.socialgo.qq.QQPlatform
import com.pingerx.socialgo.wechat.WxPlatform
import com.pingerx.socialgo.weibo.WbPlatform


/**
 * @author Pinger
 * @since 18-7-20 下午4:30
 */

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = SocialGoConfig.create(this)
                .debug(true)
                .qq(AppConstant.QQ_APP_ID)
                .wechat(AppConstant.WX_APP_ID, AppConstant.WX_APP_SECRET)
                .weibo(AppConstant.SINA_APP_KEY)

        SocialGo
                .init(config)
                .registerWxPlatform(WxPlatform.Creator())
                .registerWbPlatform(WbPlatform.Creator())
                .registerQQPlatform(QQPlatform.Creator())
                .registerAliPlatform(AliPlatform.Creator())
                .setJsonAdapter(GsonJsonAdapter())
                .setRequestAdapter(OkHttpRequestAdapter())
    }
}