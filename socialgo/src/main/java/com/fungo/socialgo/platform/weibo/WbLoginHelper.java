package com.fungo.socialgo.platform.weibo;

import android.app.Activity;
import android.content.Intent;

import com.fungo.socialgo.exception.SocialError;
import com.fungo.socialgo.listener.OnLoginListener;
import com.fungo.socialgo.listener.Recyclable;
import com.fungo.socialgo.model.LoginResult;
import com.fungo.socialgo.model.token.AccessToken;
import com.fungo.socialgo.platform.Target;
import com.fungo.socialgo.platform.weibo.model.SinaAccessToken;
import com.fungo.socialgo.platform.weibo.model.SinaUser;
import com.fungo.socialgo.util.JsonUtil;
import com.fungo.socialgo.util.SocialLogUtil;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import androidx.annotation.NonNull;

/**
 * CreateAt : 2016/12/5
 * Describe : 新浪微博登陆辅助
 *
 * @author chendong
 */

class WbLoginHelper implements Recyclable {

    public static final String TAG = WbLoginHelper.class.getSimpleName();

    private int             mLoginType;
    private OnLoginListener mOnLoginListener;
    private SsoHandler      mSsoHandler;

    WbLoginHelper(Activity context) {
        this.mSsoHandler = new SsoHandler(context);
        this.mLoginType = Target.LOGIN_WB;
    }

    /**
     * 获取用户信息
     *
     * @param token token
     */
    private void getUserInfo(final Oauth2AccessToken token) {
        JsonUtil.startJsonRequest("https://api.weibo.com/2/users/show.json?access_token=" + token.getToken() + "&uid=" + token.getUid(), SinaUser.class, new JsonUtil.Callback<SinaUser>() {
            @Override
            public void onSuccess(@NonNull SinaUser user) {
                SocialLogUtil.e(TAG, JsonUtil.getObject2Json(user));
                mOnLoginListener.onSuccess(new LoginResult(mLoginType, user, new SinaAccessToken(token)));
            }

            @Override
            public void onFailure(SocialError e) {
                mOnLoginListener.onFailure(e);
            }
        });
    }

    public void login(Activity activity, final OnLoginListener loginListener) {
        if (loginListener == null)
            return;
        this.mOnLoginListener = loginListener;
        justAuth(activity, new WbAuthListener() {
            @Override
            public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
                getUserInfo(oauth2AccessToken);
            }

            @Override
            public void cancel() {
                loginListener.onCancel();
            }

            @Override
            public void onFailure(WbConnectErrorMessage msg) {
                loginListener.onFailure(new SocialError(SocialError.CODE_SDK_ERROR, TAG + "#login#connect error," + msg.getErrorCode() + " " + msg.getErrorMessage()));
            }
        });
    }

    public void justAuth(final Activity activity, final WbAuthListener listener) {
        Oauth2AccessToken token = AccessToken.getToken(activity, AccessToken.SINA_TOKEN_KEY, Oauth2AccessToken.class);
        if (token != null && token.isSessionValid() && token.getExpiresTime() > System.currentTimeMillis()) {
            listener.onSuccess(token);
        } else {
            AccessToken.clearToken(activity, Target.LOGIN_WB);
            mSsoHandler.authorize(new WbAuthListener() {
                @Override
                public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
                    oauth2AccessToken.setBundle(null);

                    SocialLogUtil.json("test", oauth2AccessToken.toString());
                    AccessToken.saveToken(activity, AccessToken.SINA_TOKEN_KEY, oauth2AccessToken);
                    listener.onSuccess(oauth2AccessToken);
                }

                @Override
                public void cancel() {
                    listener.cancel();
                }

                @Override
                public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
                    listener.onFailure(wbConnectErrorMessage);
                }
            });
        }
    }

    public void authorizeCallBack(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }


    @Override
    public void recycle() {
        mSsoHandler = null;
    }
}
