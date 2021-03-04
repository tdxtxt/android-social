package com.pingerx.socialgo.core.uikit

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.pingerx.socialgo.core.SocialGo
import com.pingerx.socialgo.core.platform.IPlatform

/**
 * 社会化登录分享承载体，用于接收登录和分享响应的结果
 */
open class BaseActionActivity : Activity() {

    private var mIsNotFirstResume = false

    private fun getPlatform(): IPlatform? {
        val platform = SocialGo.getPlatform()
        return if (platform == null) {
            checkFinish()
            null
        } else
            platform
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for wx & dd
        getPlatform()?.handleIntent(this)
        SocialGo.activeAction(this, intent.getIntExtra(SocialGo.KEY_ACTION_TYPE, -1))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        getPlatform()?.handleIntent(this)
    }

    override fun onResume() {
        super.onResume()
        if (mIsNotFirstResume) {
            getPlatform()?.handleIntent(this)
            // 留在目标 app 后在返回会再次 resume
            checkFinish()
        } else {
            mIsNotFirstResume = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocialGo.release(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getPlatform()?.onActivityResult(requestCode, resultCode, data)
        checkFinish()
    }

    protected fun handleResp(resp: Any) {
        getPlatform()?.onResponse(resp)
        checkFinish()
    }

    protected fun handleReq(req: Any?){
        getPlatform()?.onReq(this, req)
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private fun checkFinish() {
        if (!isFinishing) {
            finish()
            overridePendingTransition(0, 0)
        }
    }
}
