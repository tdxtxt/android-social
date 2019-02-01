package com.pingerx.socialgo.core.listener

import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.model.ShareEntity

/**
 * 分享的回调
 */
interface OnShareListener {

    fun getFunction(): FunctionListener

    fun onStart(start: (shareTarget: Int, obj: ShareEntity) -> Unit) {
        getFunction().onShareStart = start
    }

    fun onSuccess(success: () -> Unit) {
        getFunction().onSuccess = success
    }

    fun onCancel(cancel: () -> Unit) {
        getFunction().onCancel = cancel
    }

    fun onFailure(failure: (error: SocialError) -> Unit) {
        getFunction().onFailure = failure
    }

    /**
     * 准备工作，在子线程执行
     */
    @Throws(Exception::class)
    fun onPrepareInBackground(shareTarget: Int, entity: ShareEntity): ShareEntity {
        return entity
    }
}
