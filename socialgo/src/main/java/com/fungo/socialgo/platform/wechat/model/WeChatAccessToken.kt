package com.fungo.socialgo.platform.wechat.model

import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target

/**
 * 微信的AccessToken
 */
class WeChatAccessToken : AccessToken() {

    val refresh_token: String? = null//用户刷新access_token。
    val scope: String? = null//用户授权的作用域，使用逗号（,）分隔
    val errcode: Int = 0
    val errmsg: String? = null

    val isNoError: Boolean
        get() = errcode == 0

    override fun loginTarget(): Int {
        return Target.LOGIN_WX
    }
}