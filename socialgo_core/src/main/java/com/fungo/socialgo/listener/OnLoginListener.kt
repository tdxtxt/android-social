package com.fungo.socialgo.listener

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.model.LoginResult

/**
 * 登录回调
 */
interface OnLoginListener {

    fun getFunction(): FunctionListener

    fun onStart(start: () -> Unit) {
        getFunction().onStart = start
    }

    fun onSuccess(success: (result: LoginResult) -> Unit) {
        getFunction().onLoginSuccess = success
    }

    fun onCancel(cancel: () -> Unit) {
        getFunction().onCancel = cancel
    }

    fun onFailure(failure: (error: SocialError) -> Unit) {
        getFunction().onFailure = failure
    }

}
