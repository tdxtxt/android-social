package com.fungo.socialgo.pay

import com.fungo.socialgo.pay.entity.PayParams
import com.fungo.socialgo.pay.entity.PayType
import com.fungo.socialgo.pay.listener.OnPayListener
import com.fungo.socialgo.pay.listener.OnPayRequestListener
import com.fungo.socialgo.pay.paystrategy.ALiPayStrategy
import com.fungo.socialgo.pay.paystrategy.PayContext
import com.fungo.socialgo.pay.paystrategy.WeChatPayStrategy
import com.fungo.socialgo.utils.SocialUtils


/**
 * @author Pinger
 * @since 18-7-20 上午11:22
 *  支付SDK封装工具类
 */

class PayApi private constructor(private var mPayParams: PayParams) {

    private var mOnPayRequestListener: OnPayRequestListener? = null
    private var mOnPayListener: OnPayListener? = null

    val weChatAppID: String?
        get() = mPayParams.weChatAppID

    fun toPay(onPayResultListener: OnPayListener) {
        mOnPayListener = onPayResultListener
        if (!SocialUtils.isNetWorkAvailable(mPayParams.activity!!.applicationContext)) {
            sendPayResult(COMMON_NETWORK_NOT_AVAILABLE_ERR)
        }
    }

    /**
     * 进行支付策略分发
     *
     * @param prePayInfo
     */
    private fun doPay(prePayInfo: String) {
        var pc: PayContext? = null
        val type = mPayParams.payWay
        val callBack = object : PayCallBack {
            override fun onPayCallBack(code: Int) {
                sendPayResult(code)
            }
        }
        when (type) {
            PayType.WxPay -> pc = PayContext(WeChatPayStrategy(mPayParams, prePayInfo, callBack))
            PayType.AliPay -> pc = PayContext(ALiPayStrategy(mPayParams, prePayInfo, callBack))
        }
        pc?.pay()
    }

    /**
     * 请求APP服务器获取预支付信息：微信，支付宝，银联都需要此步骤
     *
     * @param onPayInfoRequestListener
     * @return
     */
    fun requestPayInfo(onPayInfoRequestListener: OnPayRequestListener): PayApi {
        if (mPayParams.payWay == null) {
            throw NullPointerException("请设置支付方式")
        }

        mOnPayRequestListener = onPayInfoRequestListener
        mOnPayRequestListener!!.onPayInfoRequestStart()
        //
        //        NetworkClientInterf client = NetworkClientFactory.newClient(mPayParams.getNetworkClientType());
        //        NetworkClientInterf.CallBack callBack = new NetworkClientInterf.CallBack<String>() {
        //            @Override
        //            public void onSuccess(String result) {
        //                mOnPayRequestListener.onPayInfoRequstSuccess();
        //                doPay(result);
        //            }
        //
        //            @Override
        //            public void onFailure() {
        //                mOnPayRequestListener.onPayInfoRequestFailure();
        //                sendPayResult(COMMON_REQUEST_TIME_OUT_ERR);
        //            }
        //        };
        //
        //        HttpType type = mPayParams.getHttpType();
        //        switch (type) {
        //            case Get:
        //                client.get(mPayParams, callBack);
        //                break;
        //
        //            case Post:
        //            default:
        //                client.post(mPayParams, callBack);
        //                break;
        //        }
        return this
    }

    /**
     * 回调支付结果到请求界面
     *
     * @param code
     */
    private fun sendPayResult(code: Int) {
        when (code) {
            COMMON_PAY_OK -> mOnPayListener!!.onPaySuccess(mPayParams.payWay)

            COMMON_USER_CACELED_ERR -> mOnPayListener!!.onPayCancel(mPayParams.payWay)

            else -> mOnPayListener!!.onPayFailure(mPayParams.payWay, code)
        }
    }


    interface PayCallBack {
        fun onPayCallBack(code: Int)
    }

    companion object {

        @JvmStatic
        fun getInstance(payParams: PayParams):PayApi{
            return PayApi(payParams)
        }

        // 通用结果码
        const val COMMON_PAY_OK = 0
        const  val COMMON_PAY_ERR = -1
        const  val COMMON_USER_CACELED_ERR = -2
        const  val COMMON_NETWORK_NOT_AVAILABLE_ERR = 1
        const val COMMON_REQUEST_TIME_OUT_ERR = 2

        // 微信结果码
        const  val WECHAT_SENT_FAILED_ERR = -3
        const  val WECHAT_AUTH_DENIED_ERR = -4
        const  val WECHAT_UNSUPPORT_ERR = -5
        const  val WECHAT_BAN_ERR = -6
        const  val WECHAT_NOT_INSTALLED_ERR = -7

        // 支付宝结果码
        const  val ALI_PAY_WAIT_CONFIRM_ERR = 8000
        const   val ALI_PAY_NET_ERR = 6002
        const  val ALI_PAY_UNKNOW_ERR = 6004
        const   val ALI_PAY_OTHER_ERR = 6005

    }

}
