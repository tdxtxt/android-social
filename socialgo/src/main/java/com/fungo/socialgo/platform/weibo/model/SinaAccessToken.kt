package com.fungo.socialgo.platform.weibo.model

import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target
import com.sina.weibo.sdk.auth.Oauth2AccessToken

/**
 * 新浪的token
 */
class SinaAccessToken(token: Oauth2AccessToken) : AccessToken() {

    private val refresh_token: String
    private val phone: String

    init {
        this.openid = token.uid
        this.access_token = token.token
        this.expires_in = token.expiresTime
        this.refresh_token = token.refreshToken
        this.phone = token.phoneNum
    }

    override fun loginTarget(): Int {
        return Target.LOGIN_WB
    }
}
