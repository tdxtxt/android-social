package com.fungo.socialgo.platform.ali

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.text.TextUtils

import com.alipay.sdk.app.PayTask
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnPayListener

/**
 * 支付宝支付
 */
class Alipay(context: Context, private val params: String, private val listener: OnPayListener?) {

    private val mPayTask: PayTask = PayTask(context as Activity)

    //支付
    fun doPay() {
        val handler = Handler()
        Thread(Runnable {
            val payResult = mPayTask.payV2(params, true)
            handler.post {
                if (payResult != null) {
                    val resultStatus = payResult["resultStatus"]
                    when {
                        TextUtils.equals(resultStatus, "9000") -> //支付成功
                            listener?.onSuccess()
                        TextUtils.equals(resultStatus, "8000") -> //支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            listener?.onDealing()
                        TextUtils.equals(resultStatus, "6001") -> //支付取消
                            listener?.onCancel()
                        TextUtils.equals(resultStatus, "6002") -> //网络连接出错
                            listener?.onError(SocialError(SocialError.CODE_REQUEST_ERROR))
                        TextUtils.equals(resultStatus, "4000") -> //支付错误
                            listener?.onError(SocialError(SocialError.CODE_PAY_ERROR))
                    }
                } else {
                    listener?.onError(SocialError(SocialError.CODE_PAY_RESULT_ERROR))
                }
            }
        }).start()
    }
}
