package com.pingerx.socialgo.core.listener

import com.pingerx.socialgo.core.exception.SocialError

/**
 * @author Pinger
 * @since 2018/12/19 16:16
 * 支付回调
 */
interface OnPayListener {

    fun getFunction(): FunctionListener

    fun onStart(start: () -> Unit) {
        getFunction().onStart = start
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
     * 正在处理中 小概率事件 此时以验证服务端异步通知结果为准
     */
    fun onDealing(dealing: () -> Unit) {
        getFunction().onDealing = dealing
    }

}
