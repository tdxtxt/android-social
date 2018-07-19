package com.fungo.socialgo.pay.entity

/**
 * @author Pinger
 * @since 18-7-19 下午7:02
 * 微信支付专用
 */

data class PrePayInfo(
       var appid:String,
       var partnerid:String,
       var prepayid:String,
       var packageValue:String,
       var noncestr:String,
       var timestamp:String,
       var sign:String
)