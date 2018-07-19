package com.fungo.socialgo.share.weibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.fungo.socialgo.share.config.PlatformConfig;
import com.fungo.socialgo.share.config.SSOHandler;
import com.fungo.socialgo.share.listener.OnAuthListener;
import com.fungo.socialgo.share.listener.OnShareListener;
import com.fungo.socialgo.share.media.IShareMedia;
import com.fungo.socialgo.share.media.ShareTextImageMedia;
import com.fungo.socialgo.utils.SocialUtils;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 新浪微博 第三方Hnadler
 * Created by tsy on 16/9/18.
 */
public class SinaWBHandler extends SSOHandler {

    private Context mContext;
    private Activity mActivity;

    private SsoHandler mSsoHandler;

    private PlatformConfig.SinaWB mConfig;
    private OnAuthListener mAuthListener;
    private OnShareListener mShareListener;
    private WbShareHandler mWbShareHandler;

    private static String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";// 应用的回调页 要和微博开放平台的回调地址一致
    private final String SCOPE = "";

    /**
     * 设置微博 REDIRECT_URL
     *
     * @param redirctUrl
     */
    public static void setRedirctUrl(String redirctUrl) {
        REDIRECT_URL = redirctUrl;
    }

    @Override
    public void onCreate(Context context, PlatformConfig.Platform config) {
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mConfig = (PlatformConfig.SinaWB) config;
        WbSdk.install(context, new AuthInfo(context, mConfig.appKey, REDIRECT_URL, SCOPE));
        mWbShareHandler = new WbShareHandler((Activity) context);
        mWbShareHandler.registerApp();
    }

    @Override
    public void authorize(Activity activity, OnAuthListener authListener) {
        this.mActivity = activity;
        this.mAuthListener = authListener;

        this.mSsoHandler = new SsoHandler(activity);

        mSsoHandler.authorize(new WbAuthListener() {

            @Override
            public void onSuccess(Oauth2AccessToken accessToken) {
                // 从 Bundle 中解析 Token
                if (accessToken.isSessionValid()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("uid", accessToken.getUid());
                    map.put("access_token", accessToken.getToken());
                    map.put("refresh_token", accessToken.getRefreshToken());
                    map.put("expire_time", "" + accessToken.getExpiresTime());

                    mAuthListener.onComplete(mConfig.getName(), map);
                } else {
                    String errmsg = "errmsg=accessToken is not SessionValid";
                    SocialUtils.INSTANCE.e(errmsg);
                    mAuthListener.onError(mConfig.getName(), errmsg);
                }
            }

            @Override
            public void cancel() {
                mAuthListener.onCancel(mConfig.getName());
            }

            @Override
            public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
                String errmsg = "errmsg=" + wbConnectErrorMessage.getErrorMessage();
                SocialUtils.INSTANCE.e(errmsg);
                mAuthListener.onError(mConfig.getName(), errmsg);
            }
        });
    }

    @Override
    public void share(Activity activity, IShareMedia shareMedia, OnShareListener shareListener) {
        this.mActivity = activity;
        this.mShareListener = shareListener;

        this.mSsoHandler = new SsoHandler(activity);

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        if (shareMedia instanceof ShareTextImageMedia) {       // 文字图片分享
            ShareTextImageMedia shareTextImageMedia = (ShareTextImageMedia) shareMedia;

            if (shareTextImageMedia.getText().length() > 0) {
                TextObject textObject = new TextObject();
                textObject.text = shareTextImageMedia.getText();
                weiboMessage.textObject = textObject;
            }

            if (shareTextImageMedia.getImage() != null) {
                ImageObject imageObject = new ImageObject();
                imageObject.setImageObject(shareTextImageMedia.getImage());
                weiboMessage.imageObject = imageObject;
            }
        } else {
            if (this.mShareListener != null) {
                this.mShareListener.onError(this.mConfig.getName(), "weibo is not support this shareMedia");
            }
            return;
        }
        mWbShareHandler.shareMessage(weiboMessage, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    public void onNewIntent(Intent intent, WbShareCallback callback) {
        if (mWbShareHandler != null) {
            mWbShareHandler.doResultIntent(intent, callback);
        }
    }

    public void onResponse(int responseCode, String responseMsg) {
        switch (responseCode) {
            case WBConstants.ErrorCode.ERR_OK:
                if (this.mShareListener != null) {
                    this.mShareListener.onComplete(this.mConfig.getName());
                }
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                if (this.mShareListener != null) {
                    this.mShareListener.onCancel(this.mConfig.getName());
                }
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                if (this.mShareListener != null) {
                    this.mShareListener.onError(this.mConfig.getName(), responseMsg);
                }
                break;
        }
    }
}
