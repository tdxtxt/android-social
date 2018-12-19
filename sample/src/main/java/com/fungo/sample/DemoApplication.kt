package com.fungo.sample

import android.app.Application
import com.fungo.imagego.glide.GlideImageStrategy
import com.fungo.imagego.strategy.ImageGoEngine
import com.fungo.socialgo.SocialSdkConfig
import com.fungo.socialgo.SocialSdk



/**
 * @author Pinger
 * @since 18-7-20 下午4:30
 *
 */

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ImageGoEngine.setImageStrategy(GlideImageStrategy())

        val config = SocialSdkConfig.create(this)
                .debug(true)
                .qq(AppConstant.QQ_APP_ID)
                .wechat(AppConstant.WX_APP_ID, AppConstant.WX_APP_SECRET)
                .sina(AppConstant.SINA_APP_KEY)

        SocialSdk.init(this,config)
        SocialSdk.setJsonAdapter(GsonJsonAdapter())
        SocialSdk.setRequestAdapter(OkHttpRequestAdapter())
    }
}