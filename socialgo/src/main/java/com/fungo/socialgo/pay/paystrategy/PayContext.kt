package com.fungo.socialgo.pay.paystrategy

/**
 * @author Pinger
 * @since 18-7-19 下午6:43
 * 包裹支付策略
 */

class PayContext(private val mStrategy: PayStrategy) {

    fun pay() {
        mStrategy.doPay()
    }
}