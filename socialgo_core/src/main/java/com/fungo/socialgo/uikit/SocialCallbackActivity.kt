package com.fungo.socialgo.uikit

import android.app.Activity
import com.sina.weibo.sdk.constant.WBConstants
import com.sina.weibo.sdk.share.WbShareCallback

/**
 * 微博分享请求的回调接口
 * 微信登录请求的回调接口
 * 绑定Activity
 */
abstract class SocialCallbackActivity : Activity(), WbShareCallback {

    abstract fun handleResp(resp: Any)

    // ---------- for weibo ---------
    override fun onWbShareSuccess() {
        handleResp(WBConstants.ErrorCode.ERR_OK)
    }

    override fun onWbShareCancel() {
        handleResp(WBConstants.ErrorCode.ERR_CANCEL)
    }

    override fun onWbShareFail() {
        handleResp(WBConstants.ErrorCode.ERR_FAIL)
    }
}
