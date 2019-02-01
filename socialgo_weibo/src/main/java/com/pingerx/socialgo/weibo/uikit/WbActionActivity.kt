package com.pingerx.socialgo.weibo.uikit

import com.pingerx.socialgo.uikit.BaseActionActivity
import com.sina.weibo.sdk.constant.WBConstants
import com.sina.weibo.sdk.share.WbShareCallback

/**
 * @author Pinger
 * @since 2019/1/31 15:15
 */
class WbActionActivity : BaseActionActivity(), WbShareCallback {

    override fun onWbShareFail() {
        handleResp(WBConstants.ErrorCode.ERR_FAIL)
    }

    override fun onWbShareCancel() {
        handleResp(WBConstants.ErrorCode.ERR_CANCEL)
    }

    override fun onWbShareSuccess() {
        handleResp(WBConstants.ErrorCode.ERR_OK)
    }
}