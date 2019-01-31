package com.fungo.socialgo.platform

import android.app.Activity
import android.content.Context

import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.listener.PlatformLifecycle
import com.fungo.socialgo.model.ShareEntity


/**
 * 定义第三方平台接口协议
 */
interface IPlatform : PlatformLifecycle {

    /**
     * 获取中间页的Clazz
     */
    fun getActionClazz(): Class<*>

    /**
     * 检测参数配置
     */
    fun checkPlatformConfig(): Boolean

    /**
     * 初始化分享监听
     */
    fun initOnShareListener(listener: OnShareListener)

    /**
     * 是否安装
     */
    fun isInstall(context: Context): Boolean

    /**
     * 发起登录
     */
    fun login(activity: Activity, listener: OnLoginListener)

    /**
     * 发起分享
     */
    fun share(activity: Activity, target: Int, entity: ShareEntity)

    /**
     * 支付
     */
    fun doPay(context: Context, params: String, listener: OnPayListener)
}
