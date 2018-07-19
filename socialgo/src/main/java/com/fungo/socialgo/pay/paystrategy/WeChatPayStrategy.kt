package com.fungo.socialgo.pay.paystrategy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.fungo.socialgo.pay.PayApi
import com.fungo.socialgo.pay.entity.PayParams
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * @author Pinger
 * @since 18-7-19 下午7:13
 *
 */

class WeChatPayStrategy(private val params:PayParams, private val prePayInfo:String,
                        private val resultListener:PayApi.PayCallBack):PayStrategy {

    private var mBroadcastManager: LocalBroadcastManager?=null
    private var mContext: Context = params.activity as Context


    companion object {
        const val WECHAT_PAY_RESULT_ACTION:String  = "com.tencent.mm.opensdk.WECHAT_PAY_RESULT_ACTION";
        const val WECHAT_PAY_RESULT_EXTRA:String = "com.tencent.mm.opensdk.WECHAT_PAY_RESULT_EXTRA";
    }


    override fun doPay() {
        val wxApi = WXAPIFactory.createWXAPI(mContext.applicationContext, params.weChatAppID, true)
        if (!wxApi.isWXAppInstalled) {
            resultListener.onPayCallBack(PayApi.WECHAT_NOT_INSTALLED_ERR)
            return
        }

        if (!wxApi.isWXAppSupportAPI) {
            resultListener.onPayCallBack(PayApi.WECHAT_UNSUPPORT_ERR)
            return
        }
        wxApi.registerApp(params.weChatAppID)
        registerPayResultBroadcast()

        // TODO 需要做正式解析，修改PrePayInfo.java类，并解开此处注释
        /*Gson gson = new Gson();
        PrePayInfo payInfo = gson.fromJson(mPrePayInfo, PrePayInfo.class);
        PayReq req = new PayReq();
        req.appId = payInfo.appid;
        req.partnerId = payInfo.partnerid;
        req.prepayId = payInfo.prepayid;
        req.packageValue = payInfo.packageValue;
        req.nonceStr = payInfo.noncestr;
        req.timeStamp = payInfo.timestamp;
        req.sign = payInfo.sign;

        // 发送支付请求：跳转到微信客户端
        wxapi.sendReq(req);*/
    }


    private fun registerPayResultBroadcast() {
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext.applicationContext)
        val filter = IntentFilter(WECHAT_PAY_RESULT_ACTION)
        mBroadcastManager?.registerReceiver(mReceiver, filter)
    }


    private fun  unRegisterPayResultBroadcast() {
        if (mBroadcastManager != null) {
            mBroadcastManager?.unregisterReceiver(mReceiver)
            mBroadcastManager = null
        }
    }

    private val  mReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val  result = intent?.getIntExtra(WECHAT_PAY_RESULT_EXTRA, -100)?:0
            resultListener.onPayCallBack(result)
            unRegisterPayResultBroadcast()
        }
    }
}