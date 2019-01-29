package com.fungo.socialgo.manager

import android.app.Activity
import android.content.Context

import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.Target

/**
 * 第三方平台操作管理器，在流程结束后会回收所有资源
 */
object PlatformManager {

    const val INVALID_PARAM = -1

    const val ACTION_TYPE_LOGIN = 0
    const val ACTION_TYPE_SHARE = 1
    const val ACTION_TYPE_PAY = 2

    const val KEY_SHARE_MEDIA_OBJ = "KEY_SHARE_MEDIA_OBJ"  // media obj key
    const val KEY_ACTION_TYPE = "KEY_ACTION_TYPE"          // action type
    const val KEY_PAY_PARAMS = "KEY_PAY_PARAMS"            // pay params
    const val KEY_SHARE_TARGET = "KEY_SHARE_TARGET"        // share target
    const val KEY_LOGIN_TARGET = "KEY_LOGIN_TARGET"        // login target

    private var platform: IPlatform? = null

    fun makePlatform(context: Context, target: Int): IPlatform {
        val platformTarget = Target.mapPlatform(target)
        val platform = SocialSdk.getPlatform(context, platformTarget)
                ?: throw IllegalArgumentException(Target.toDesc(target) + "  创建platform失败，请检查参数 " + SocialSdk.getConfig().toString())
        this.platform = platform
        return platform
    }

    fun release(activity: Activity?) {
        platform?.recycle()
        platform = null
        if (activity != null && !activity.isFinishing) {
            activity.finish()
        }
    }

    fun action(activity: Activity, actionType: Int) {
        if (actionType != -1) {
            when (actionType) {
                PlatformManager.ACTION_TYPE_LOGIN -> LoginManager.activeLogin(activity)
                PlatformManager.ACTION_TYPE_SHARE -> ShareManager.activeShare(activity)
                PlatformManager.ACTION_TYPE_PAY -> PayManager.activePay(activity)
            }
        }
    }

    fun getPlatform(): IPlatform? {
        return platform
    }
}
