package com.fungo.socialgo.listener

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.model.LoginResult

/**
 * 登录回调
 */
interface OnLoginListener {

    fun onStart()

    fun onSuccess(loginResult: LoginResult)

    fun onCancel()

    fun onFailure(e: SocialError)
}
