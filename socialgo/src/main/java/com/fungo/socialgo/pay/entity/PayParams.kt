package com.fungo.socialgo.pay.entity

import android.app.Activity

/**
 * @author Pinger
 * @since 2018/4/30 20:09
 * 支付接口请求的参数实体
 */

class PayParams {

    var activity: Activity? = null
        private set
    var weChatAppID: String? = null
        private set
    var payWay: PayType? = null
        private set
    var goodsPrice: Int = 0
        private set
    var goodsName: String? = null
        private set
    var goodsIntroduction: String? = null
        private set
    var apiUrl: String? = null
        private set

    private fun setWechatAppID(id: String) {
        weChatAppID = id
    }

    class Builder(activity: Activity) {

        private var params = PayParams()

        init {
            params.activity = activity
        }

        fun wechatAppID(appid: String): Builder {
            params.setWechatAppID(appid)
            return this
        }

        fun goodsPrice(price: Int): Builder {
            params.goodsPrice = price
            return this
        }

        fun goodsName(name: String): Builder {
            params.goodsName = name
            return this
        }

        fun goodsIntroduction(introduction: String): Builder {
            params.goodsIntroduction = introduction
            return this
        }

        fun requestBaseUrl(url: String): Builder {
            params.apiUrl = url
            return this
        }

        fun build(): PayParams {
            return params
        }

    }

}