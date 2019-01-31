package com.fungo.socialgo.weibo

import android.app.Activity
import android.text.TextUtils
import com.fungo.socialgo.SocialGo
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
                SocialGo.getExecutor().execute {
                    val params = HashMap<String, String>()
                    params["access_token"] = token.token
                    params["status"] = obj.getSummary()
                    val data = SocialGo.getRequestAdapter().postData("https://api.weibo.com/2/statuses/share.json", params, "pic", obj.getThumbImagePath())
                    SocialGo.getHandler().post {
                        if (TextUtils.isEmpty(data)) {
                            listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PARSE_ERROR, "open api 分享失败 $data"))
                        } else {
                            val jsonObject = JSONObject(data)
                            if (jsonObject.has("id") && jsonObject.get("id") != null) {
                                listener?.getFunction()?.onSuccess?.invoke()
                            } else {
                                listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PARSE_ERROR, "open api 分享失败 $data"))
                            }
                        }
                    }
                }
            }
        })
    }

    open inner class WbAuthListenerImpl : WbAuthListener {
        override fun onSuccess(token: Oauth2AccessToken) {}

        override fun cancel() {
            listener?.getFunction()?.onCancel?.invoke()
        }

        override fun onFailure(msg: WbConnectErrorMessage) {
            listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "#WbAuthListenerImpl#wb auth fail," + msg.errorCode + " " + msg.errorMessage))
        }
    }

}
