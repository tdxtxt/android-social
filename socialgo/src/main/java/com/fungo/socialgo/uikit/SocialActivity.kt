package com.fungo.socialgo.uikit

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle

import com.fungo.socialgo.manager.PlatformManager
import com.fungo.socialgo.platform.IPlatform


/**
 * 社会化登录分享承载体，用于接收登录和分享响应的结果
 */
class SocialActivity : SocialCallbackActivity() {

    private var mIsNotFirstResume = false

    private val platform: IPlatform?
        get() {
            val platform = PlatformManager.getPlatform()
            return if (platform == null) {
                checkFinish()
                null
            } else
                platform
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for wx & dd
        platform?.handleIntent(this)
        PlatformManager.action(this, intent.getIntExtra(PlatformManager.KEY_ACTION_TYPE, -1))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        platform?.handleIntent(this)
    }

    override fun onResume() {
        super.onResume()
        if (mIsNotFirstResume) {
            platform?.handleIntent(this)
            // 留在目标 app 后在返回会再次 resume
            checkFinish()
        } else {
            mIsNotFirstResume = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlatformManager.release(this)
    }

    override fun handleResp(resp: Any) {
        val platform = platform
        platform?.onResponse(resp)
        checkFinish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        platform?.onActivityResult(requestCode, resultCode, data)
        checkFinish()
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private fun checkFinish() {
        if (!isFinishing) {
            finish()
            overridePendingTransition(0, 0)
        }
    }
}
