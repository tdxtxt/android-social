package com.fungo.socialgo.wechat.uikit

import com.fungo.socialgo.uikit.BaseSocialActivity
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * @author Pinger
 * @since 2019/1/31 14:54
 */
class WxActionActivity : BaseSocialActivity(), IWXAPIEventHandler {

    override fun onResp(resp: BaseResp) {
        handleResp(resp)
    }

    override fun onReq(p0: BaseReq?) {
    }
}