package com.fungo.socialgo.pay.entity

/**
 * @author Pinger
 * @since 2018/4/30 20:06
 * 支付类型，目前有支付宝和微信两种方式
 */
enum class PayType constructor(private var payType: Int) {
    WxPay(0),
    AliPay(1);

    override fun toString(): String {
        return payType.toString()
    }
}
