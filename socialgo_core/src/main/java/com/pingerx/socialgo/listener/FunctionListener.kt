package com.pingerx.socialgo.listener

import com.pingerx.socialgo.exception.SocialError
import com.pingerx.socialgo.model.LoginResult
import com.pingerx.socialgo.model.ShareEntity

/**
 * @author Pinger
 * @since 2019/1/30 15:49
 *
 * 函数变量申明
 */
class FunctionListener {

    var onStart: (() -> Unit)? = null

    var onShareStart: ((shareTarget: Int, obj: ShareEntity) -> Unit)? = null

    var onSuccess: (() -> Unit)? = null

    var onLoginSuccess: ((loginResult: LoginResult) -> Unit)? = null

    var onFailure: ((error: SocialError) -> Unit)? = null

    var onDealing: (() -> Unit)? = null

    var onCancel: (() -> Unit)? = null
}
