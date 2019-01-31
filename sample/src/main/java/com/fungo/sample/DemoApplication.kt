package com.fungo.sample

import android.app.Application
import com.fungo.imagego.ImageGo
import com.fungo.imagego.glide.GlideImageStrategy
import com.fungo.socialgo.SocialGo
import com.fungo.socialgo.SocialGoConfig
import com.fungo.socialgo.qq.QQPlatform
import com.fungo.socialgo.wechat.WxPlatform
import com.fungo.socialgo.weibo.WbPlatform


/**
 * @author Pinger
 * @since 18-7-20 下午4:30
 */

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ImageGo.setStrategy(GlideImageStrategy())

        val config = SocialGoConfig.create(this)
                .debug(true)
                .qq(AppConstant.QQ_APP_ID)
                .wechat(AppConstant.WX_APP_ID, AppConstant.WX_APP_SECRET)
                .sina(AppConstant.SINA_APP_KEY)

        SocialGo
                .init(config)
                .registerWxPlatform(WxPlatform.Creator())
                .registerWbPlatform(WbPlatform.Creator())
                .registerQQPlatform(QQPlatform.Creator())
                .setJsonAdapter(GsonJsonAdapter())
                .setRequestAdapter(OkHttpRequestAdapter())
    }
}