package com.pingerx.socialgo.qq.model

import com.pingerx.socialgo.core.model.token.AccessToken
import com.pingerx.socialgo.core.platform.Target


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
