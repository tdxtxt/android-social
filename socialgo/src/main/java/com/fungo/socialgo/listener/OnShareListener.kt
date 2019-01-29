package com.fungo.socialgo.listener

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.model.ShareEntity

/**
 * 分享的回调
 */
interface OnShareListener {

    fun onStart(shareTarget: Int, obj: ShareEntity)

    /**
     * 准备工作，在子线程执行
     */
    @Throws(Exception::class)
    fun onPrepareInBackground(shareTarget: Int, obj: ShareEntity): ShareEntity?

    fun onSuccess()

    fun onFailure(e: SocialError)

    fun onCancel()
}
