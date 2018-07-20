package com.fungo.socialgo.share.listener

import com.fungo.socialgo.share.config.PlatformType

/**
 * @author Pinger
 * @since 18-7-20 上午9:55
 * 登录授权回调
 */

interface OnAuthListener {

    /**
     * 登录完成
     */
    fun onComplete(platform_type: PlatformType, map: Map<String, String>)

    /**
     * 登录错误
     */
    fun onError(platform_type: PlatformType, err_msg: String)

    /**
     * 登录取消
     */
    fun onCancel(platform_type: PlatformType)
}