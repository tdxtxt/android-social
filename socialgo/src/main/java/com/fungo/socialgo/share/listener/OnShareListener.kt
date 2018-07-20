package com.fungo.socialgo.share.listener

import com.fungo.socialgo.share.config.PlatformType

/**
 * @author Pinger
 * @since 18-7-20 上午9:56
 * 分享回调
 */

interface OnShareListener {

    /**
     * 分享完成
     */
    fun onComplete(platform_type: PlatformType)

    /**
     * 分享完成
     */
    fun onError(platform_type: PlatformType, err_msg: String)

    /**
     * 分享完成
     */
    fun onCancel(platform_type: PlatformType)

}