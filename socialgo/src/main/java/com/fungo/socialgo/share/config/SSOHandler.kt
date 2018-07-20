package com.fungo.socialgo.share.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.fungo.socialgo.share.listener.OnAuthListener
import com.fungo.socialgo.share.listener.OnShareListener
import com.fungo.socialgo.share.media.IShareMedia

/**
 * @author Pinger
 * @since 18-7-20 上午9:43
 * 社会化API接口
 */

abstract class SSOHandler {


    /**
     * 是否安装
     * @return
     */
    open fun isInstall(): Boolean {
        return false
    }

    /**
     * 初始化
     * @param config 配置信息
     */
    open fun onCreate(context: Context, config: PlatformConfig.Platform) {

    }

    /**
     * 登录授权
     * @param activity
     * @param authListener 授权回调
     */
    open fun authorize(activity: Activity, authListener: OnAuthListener) {

    }

    /**
     * 分享
     * @param shareMedia 分享内容
     * @param shareListener 分享回调
     */
    open fun share(activity: Activity, shareMedia: IShareMedia, shareListener: OnShareListener) {

    }

    /**
     * 重写onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

}