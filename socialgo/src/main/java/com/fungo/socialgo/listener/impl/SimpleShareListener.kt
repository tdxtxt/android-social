package com.fungo.socialgo.listener.impl

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.model.ShareEntity

/**
 * 分享回调简单实现
 */
class SimpleShareListener : OnShareListener {

    override fun onStart(shareTarget: Int, obj: ShareEntity) {}

    @Throws(Exception::class)
    override fun onPrepareInBackground(shareTarget: Int, obj: ShareEntity): ShareEntity {
        return obj
    }

    override fun onSuccess() {

    }

    override fun onFailure(e: SocialError) {

    }

    override fun onCancel() {

    }
}
