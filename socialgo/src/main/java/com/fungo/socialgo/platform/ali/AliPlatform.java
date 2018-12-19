package com.fungo.socialgo.platform.ali;

import android.app.Activity;
import android.content.Context;

import com.fungo.socialgo.SocialSdk;
import com.fungo.socialgo.listener.OnPayListener;
import com.fungo.socialgo.model.ShareObj;
import com.fungo.socialgo.platform.AbsPlatform;
import com.fungo.socialgo.platform.IPlatform;
import com.fungo.socialgo.platform.PlatformCreator;

/**
 * @author Pinger
 * @since 2018/12/19 17:12
 */
public class AliPlatform extends AbsPlatform {

    public AliPlatform(String appId, String appName) {
        super(appId, appName);
    }

    @Override
    public void doPay(Context context, String payParams, OnPayListener listener) {
        new Alipay(context, payParams, listener).doPay();
    }

    public static class Creator implements PlatformCreator {
        @Override
        public IPlatform create(Context context, int target) {
            return new AliPlatform("", SocialSdk.getConfig().getAppName());
        }
    }

    @Override
    protected void shareOpenApp(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareText(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareImage(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareApp(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareWeb(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareMusic(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    protected void shareVideo(int shareTarget, Activity activity, ShareObj obj) {

    }

    @Override
    public void recycle() {

    }
}
