package com.fungo.socialgo.wechat.pay

import android.text.TextUtils

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnPayListener
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI

import org.json.JSONException
import org.json.JSONObject

/**
 * 微信支付
 */
class WXPay(private val wxApi: IWXAPI) {

    private var mListener: OnPayListener? = null

    /**
     * 发起微信支付
     */
    fun doPay(param: String, listener: OnPayListener?) {
        mListener = listener

        if (!check()) {
            listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_NOT_INSTALL))
            return
        }

        val json: JSONObject
        try {
            json = JSONObject(param)
        } catch (e: JSONException) {
            e.printStackTrace()
            listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PAY_PARAM_ERROR))
            return
        }

        if (TextUtils.isEmpty(json.optString("appid")) || TextUtils.isEmpty(json.optString("partnerid"))
                || TextUtils.isEmpty(json.optString("prepayid")) || TextUtils.isEmpty(json.optString("package")) ||
                TextUtils.isEmpty(json.optString("noncestr")) || TextUtils.isEmpty(json.optString("timestamp")) ||
                TextUtils.isEmpty(json.optString("sign"))) {
            listener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PAY_PARAM_ERROR))
            return
        }

        val req = PayReq()
        req.appId = json.optString("appid")
        req.partnerId = json.optString("partnerid")
        req.prepayId = json.optString("prepayid")
        req.packageValue = json.optString("package")
        req.nonceStr = json.optString("noncestr")
        req.timeStamp = json.optString("timestamp")
        req.sign = json.optString("sign")
        wxApi.sendReq(req)
    }

    //支付回调响应
    fun onResp(error_code: Int) {
        when (error_code) {
            0 -> //成功
                mListener?.getFunction()?.onSuccess?.invoke()
            -1 -> //错误
                mListener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PAY_ERROR))
            -2 -> //取消
                mListener?.getFunction()?.onCancel?.invoke()
        }
        mListener = null
    }

    //检测是否支持微信支付
    private fun check(): Boolean {
        return wxApi.isWXAppInstalled && wxApi.wxAppSupportAPI >= Build.PAY_SUPPORTED_SDK_INT
    }

    companion object {
        private var instance: WXPay? = null
        fun getInstance(): WXPay {
            if (instance == null) {
                throw IllegalAccessException("请先调用initWxApi初始化支付接口")
            }
            return instance!!
        }

        fun initWxApi(wxApi: IWXAPI) {
            if (instance == null) {
                instance = WXPay(wxApi)
            }
        }
    }
}
