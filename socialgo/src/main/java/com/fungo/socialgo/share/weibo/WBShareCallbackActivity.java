package com.fungo.socialgo.share.weibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.fungo.socialgo.share.config.PlatformConfig;
import com.fungo.socialgo.share.config.PlatformType;
import com.fungo.socialgo.share.SocialApi;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.share.WbShareCallback;


/**
 * Created by tsy on 16/8/4.
 */
public class WBShareCallbackActivity extends Activity implements WbShareCallback {

    protected SinaWBHandler mSinaWBHandler = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocialApi api = SocialApi.get(this.getApplicationContext());
        this.mSinaWBHandler = (SinaWBHandler) api.getSSOHandler(PlatformType.SINA_WB);
        this.mSinaWBHandler.onCreate(this, PlatformConfig.getPlatformConfig(PlatformType.SINA_WB));

        if (this.getIntent() != null) {
            this.handleIntent(this.getIntent());
        }
    }

    protected final void onNewIntent(Intent paramIntent) {
        super.onNewIntent(paramIntent);
        SocialApi api = SocialApi.get(this.getApplicationContext());
        this.mSinaWBHandler = (SinaWBHandler) api.getSSOHandler(PlatformType.SINA_WB);
        this.mSinaWBHandler.onCreate(this, PlatformConfig.getPlatformConfig(PlatformType.SINA_WB));

        this.handleIntent(this.getIntent());
    }

    protected void handleIntent(Intent intent) {
        this.mSinaWBHandler.onNewIntent(intent, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mSinaWBHandler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onWbShareSuccess() {
        this.mSinaWBHandler.onResponse(WBConstants.ErrorCode.ERR_OK, "");
        finish();
    }

    @Override
    public void onWbShareCancel() {
        this.mSinaWBHandler.onResponse(WBConstants.ErrorCode.ERR_CANCEL, "");
        finish();
    }

    @Override
    public void onWbShareFail() {
        this.mSinaWBHandler.onResponse(WBConstants.ErrorCode.ERR_FAIL, "onWbShareFail");
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
