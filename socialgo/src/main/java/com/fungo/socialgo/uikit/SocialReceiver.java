package com.fungo.socialgo.uikit;

import android.app.Activity;

import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

// weibo
// wechat

/**
 * CreateAt : 2018/8/21
 * Describe : 屏蔽平台对于 ActionActivity 的差异性
 *
 * @author chendong
 */
public abstract class SocialReceiver extends Activity implements
        WbShareCallback,
        IWXAPIEventHandler {

    abstract void handleResp(Object resp);

    // ---------- for weibo ---------
    @Override
    public void onWbShareSuccess() {
        handleResp(WBConstants.ErrorCode.ERR_OK);
    }

    @Override
    public void onWbShareCancel() {
        handleResp(WBConstants.ErrorCode.ERR_CANCEL);
    }

    @Override
    public void onWbShareFail() {
        handleResp(WBConstants.ErrorCode.ERR_FAIL);
    }

    // ---------- for wechat ---------
    @Override
    public void onResp(BaseResp resp) {
        handleResp(resp);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

}
