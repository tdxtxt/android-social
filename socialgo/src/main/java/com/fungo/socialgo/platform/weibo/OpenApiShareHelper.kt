package com.fungo.socialgo.platform.weibo

import android.app.Activity
import android.text.TextUtils
import bolts.Continuation
import bolts.Task
import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.model.ShareEntity
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import org.json.JSONObject
import java.util.*

/**
 * openApi 分享动图
 */
class OpenApiShareHelper constructor(private val loginHelper: WbLoginHelper, private val listener: OnShareListener?) {

    fun post(activity: Activity, obj: ShareEntity) {
        loginHelper.justAuth(activity, object : WbAuthListenerImpl() {
            override fun onSuccess(token: Oauth2AccessToken) {
                Task.callInBackground {
                    val params = HashMap<String, String>()
                    params["access_token"] = token.token
                    params["status"] = obj.getSummary()
                    SocialSdk.getRequestAdapter().postData("https://api.weibo.com/2/statuses/share.json", params, "pic", obj.getThumbImagePath())
                }.continueWith(Continuation<String?, Boolean> { task ->
                    if (task.isFaulted || TextUtils.isEmpty(task.result)) {
                        throw SocialError(SocialError.CODE_PARSE_ERROR, "open api 分享失败 " + task.result).exception(task.error)
                    } else {
                        val jsonObject = JSONObject(task.result)
                        if (jsonObject.has("id") && jsonObject.get("id") != null) {
                            listener?.onSuccess()
                            true
                        } else {
                            throw SocialError(SocialError.CODE_PARSE_ERROR, "open api 分享失败 " + task.result)
                        }
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(Continuation<Boolean, Boolean> { task ->
                    if (task != null && task.isFaulted) {
                        val error = task.error
                        if (error is SocialError) {
                            listener?.onFailure(error)
                        } else {
                            listener?.onFailure(SocialError(SocialError.CODE_REQUEST_ERROR, "open api 分享失败").exception(error))
                        }
                    }
                    true
                }, Task.UI_THREAD_EXECUTOR)
            }
        })
    }


    open inner class WbAuthListenerImpl : WbAuthListener {
        override fun onSuccess(token: Oauth2AccessToken) {}

        override fun cancel() {
            listener?.onCancel()
        }

        override fun onFailure(msg: WbConnectErrorMessage) {
            listener?.onFailure(SocialError(SocialError.CODE_SDK_ERROR, "#WbAuthListenerImpl#wb auth fail," + msg.errorCode + " " + msg.errorMessage))
        }
    }

}
