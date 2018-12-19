package com.fungo.socialgo.social.listener;

import com.fungo.socialgo.social.exception.SocialError;
import com.fungo.socialgo.social.model.LoginResult;

/**
 * CreateAt : 2016/12/25
 * Describe : 登陆监听
 *
 * @author chendong
 */
public interface OnLoginListener {

    void onStart();

    void onSuccess(LoginResult loginResult);

    void onCancel();

    void onFailure(SocialError e);
}
