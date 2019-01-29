package com.fungo.socialgo.platform.qq.model

import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target


/**
 * QQ登录的AccessToken
 */
class QQAccessToken : AccessToken() {

    private val ret: Int = 0
    private val pay_token: String? = null
    private val pf: String? = null
    private val pfkey: String? = null
    private val msg: String? = null
    private val login_cost: String? = null
    private val query_authority_cost: String? = null
    private val authority_cost: String? = null

    override fun loginTarget(): Int {
        return Target.LOGIN_QQ
    }
}
