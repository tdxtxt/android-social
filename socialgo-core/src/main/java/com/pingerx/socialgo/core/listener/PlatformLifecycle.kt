package com.pingerx.socialgo.core.listener

import android.app.Activity
import android.content.Intent

/**
 * 各个平台执行的生命周期绑定
 */
interface PlatformLifecycle : Recyclable {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun handleIntent(activity: Activity)

    fun onResponse(resp: Any)

    fun onReq(activity: Activity, req: Any?)
}