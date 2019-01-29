package com.fungo.socialgo.listener

import com.fungo.socialgo.exception.SocialError

/**
 * @author Pinger
 * @since 2018/12/19 16:16
 * 支付回调
 */
interface OnPayListener {

    fun onStart()

    /**
     * 支付成功
     */
    fun onSuccess()

    /**
     * 正在处理中 小概率事件 此时以验证服务端异步通知结果为准
     */
    fun onDealing()

    fun onError(error: SocialError)

    fun onCancel()
}
