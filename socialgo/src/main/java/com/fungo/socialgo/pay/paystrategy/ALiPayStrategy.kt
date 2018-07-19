package com.fungo.socialgo.pay.paystrategy

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import com.alipay.sdk.app.PayTask
import com.fungo.socialgo.pay.PayApi
import com.fungo.socialgo.pay.entity.ALiPayResult
import com.fungo.socialgo.pay.entity.PayParams
import com.fungo.socialgo.utils.SocialUtils

/**
 * @author Pinger
 * @since 18-7-19 下午6:45
 * 支付宝支付策略
 */

class ALiPayStrategy(private val params:PayParams, private val prePayInfo:String,
                     private val resultListener:PayApi.PayCallBack):PayStrategy {

    companion object {
        private const val PAY_RESULT_MSG:Int = 0
    }

    private  val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what != PAY_RESULT_MSG) {
                return
            }
            SocialUtils.singlePool.shutdown()
            val result = ALiPayResult(msg.obj as Map<String, String>)

            when (result.getResultStatus()) {
                ALiPayResult.PAY_OK_STATUS -> resultListener.onPayCallBack(PayApi.COMMON_PAY_OK)
                ALiPayResult.PAY_CANCLE_STATUS -> resultListener.onPayCallBack(PayApi.COMMON_USER_CACELED_ERR)
                ALiPayResult.PAY_FAILED_STATUS -> resultListener.onPayCallBack(PayApi.COMMON_PAY_ERR)
                ALiPayResult.PAY_WAIT_CONFIRM_STATUS -> resultListener.onPayCallBack(PayApi.ALI_PAY_WAIT_CONFIRM_ERR)
                ALiPayResult.PAY_NET_ERR_STATUS -> resultListener.onPayCallBack(PayApi.ALI_PAY_NET_ERR)
                ALiPayResult.PAY_UNKNOWN_ERR_STATUS -> resultListener.onPayCallBack(PayApi.ALI_PAY_UNKNOW_ERR)
                else -> resultListener.onPayCallBack(PayApi.ALI_PAY_OTHER_ERR)
            }
            //mHandler.removeCallbacksAndMessages(null)
        }
    }


    override fun doPay() {
        val payRun = Runnable {
            val task = PayTask(params.activity)

            // TODO 请根据自身需求解析mPrePayinfo，最终的字符串值应该为一连串key=value形式
            val  result = task.payV2 (prePayInfo, true)
            val message = mHandler . obtainMessage ()

            // TODO 直接在这里回调到主线程
            message.what = PAY_RESULT_MSG
            message.obj = result
            mHandler.sendMessage(message)
        }
        SocialUtils.singlePool.execute(payRun)
    }
}