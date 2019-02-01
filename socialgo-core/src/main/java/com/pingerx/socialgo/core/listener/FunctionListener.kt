package com.pingerx.socialgo.core.listener

import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.model.LoginResult
import com.pingerx.socialgo.core.model.ShareEntity

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
