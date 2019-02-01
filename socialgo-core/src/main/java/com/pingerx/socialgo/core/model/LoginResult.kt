package com.pingerx.socialgo.core.model

import com.pingerx.socialgo.core.model.token.AccessToken
import com.pingerx.socialgo.core.model.user.BaseSocialUser

/**
 * 登陆结果,包括登录类型，登录成功返回的用户信息，token等信息
 */
class LoginResult {

    // 登陆的类型，对应 Target.LOGIN_QQ 等。。。
    var type: Int = 0
    // 返回的基本用户信息
    // 针对登录类型可强转为 WbUser,WxUser,QQUser 来获取更加丰富的信息
    var socialUser: BaseSocialUser? = null
    // 本次登陆的 token 信息，openid,unionid,token,expires_in
    var accessToken: AccessToken? = null
    // 微信授权码
    var wxAuthCode: String? = null

    constructor(type: Int, baseUser: BaseSocialUser, baseToken: AccessToken?) {
        this.type = type
        socialUser = baseUser
        accessToken = baseToken
    }

    constructor(type: Int, wxAuthCode: String) {
        this.type = type
        this.wxAuthCode = wxAuthCode
    }

    override fun toString(): String {
        return "LoginResult{" +
                "type=" + type +
                ", socialUser=" + socialUser +
                ", accessToken=" + accessToken +
                ", wxAuthCode='" + wxAuthCode + '\''.toString() +
                '}'.toString()
    }
}
