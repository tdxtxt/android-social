package com.fungo.socialgo.listener.impl;

import com.fungo.socialgo.exception.SocialError;
import com.fungo.socialgo.listener.OnShareListener;
import com.fungo.socialgo.model.ShareObj;

/**
 * CreateAt : 2017/5/22
 * Describe : 简化版本分享监听
 *
 * @author chendong
 */
public class SimpleShareListener implements OnShareListener {

    @Override
    public void onStart(int shareTarget, ShareObj obj) {
    }

    @Override
    public ShareObj onPrepareInBackground(int shareTarget, ShareObj obj) throws Exception {
        return obj;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(SocialError e) {

    }

    @Override
    public void onCancel() {

    }
}
