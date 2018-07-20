package com.fungo.socialgo.share.weixin

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.fungo.socialgo.share.config.PlatformConfig
import com.fungo.socialgo.share.config.PlatformType
import com.fungo.socialgo.share.config.SSOHandler
import com.fungo.socialgo.share.listener.OnAuthListener
import com.fungo.socialgo.share.listener.OnShareListener
import com.fungo.socialgo.utils.SocialUtils
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.*

/**
 * @author Pinger
 * @since 18-7-20 下午4:06
 * 微信的回调处理
 */

class WXHandler : SSOHandler() {

    private var mContext: Context? = null
    private var mActivtiy: Activity? = null

    private var mWXApi: IWXAPI? = null

    //默认scope 和 state
    private var sScope = "snsapi_userinfo,snsapi_friend,snsapi_message"
    private var sState = "none"

    private val mEventHandler: IWXAPIEventHandler
    private var mLastTransaction = ""

    private var mConfig: PlatformConfig.Weixin? = null
    private var mAuthListener: OnAuthListener? = null
    private var mShareListener: OnShareListener? = null

    init {
        this.mEventHandler = object : IWXAPIEventHandler {
            override fun onResp(resp: BaseResp) {
                if (mLastTransaction != resp.transaction) {
                    return
                }

                val type = resp.type
                when (type) {
                //授权返回
                    ConstantsAPI.COMMAND_SENDAUTH -> this@WXHandler.onAuthCallback(resp as SendAuth.Resp)
                //分享返回
                    ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX -> this@WXHandler.onShareCallback(resp as SendMessageToWX.Resp)
                }

            }

            override fun onReq(req: BaseReq) {}
        }
    }


    /**
     * 设置scope和state
     * @param scope
     * @param state
     */
    fun setScopeState(scope: String, state: String) {
        sScope = scope
        sState = state
    }

    override fun onCreate(context: Context, config: PlatformConfig.Platform) {
        this.mContext = context
        this.mConfig = config as PlatformConfig.Weixin

        this.mWXApi = WXAPIFactory.createWXAPI(mContext?.applicationContext, this.mConfig?.appId)
        this.mWXApi!!.registerApp(this.mConfig?.appId)
    }


    override fun authorize(activity: Activity, authListener: OnAuthListener) {
        if (!isInstall()) {
            authListener.onError(getPlatformType(), "wx not install")
            SocialUtils.e("wx not install")
            return
        }

        this.mActivtiy = activity
        this.mAuthListener = authListener

        val req1 = SendAuth.Req()
        req1.scope = sScope
        req1.state = sState
        req1.transaction = buildTransaction("authorize")
        mLastTransaction = req1.transaction

        if (this.mWXApi?.sendReq(req1) == false) {
            this.mAuthListener?.onError(getPlatformType(), "sendReq fail")
            SocialUtils.e("wxapi sendReq fail")
        }
    }


    /**
     * 验证授权回调
     */
    private fun onAuthCallback(resp: SendAuth.Resp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK       //授权成功
            -> {
                val data = HashMap<String, String>()
                data["code"] = resp.code
                this.mAuthListener?.onComplete(PlatformType.WEIXIN, data)
            }

            BaseResp.ErrCode.ERR_USER_CANCEL      //授权取消
            -> if (this.mAuthListener != null) {
                this.mAuthListener?.onCancel(PlatformType.WEIXIN)
            }

            else    //授权失败
            -> {
                val err = TextUtils.concat("weixin auth error (", resp.errCode.toString(), "):", resp.errStr)
                mAuthListener?.onError(PlatformType.WEIXIN, err.toString())
            }
        }
    }

    /**
     * 验证分享回调
     */
    private fun onShareCallback(resp: SendMessageToWX.Resp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK       //分享成功
            -> if (this.mShareListener != null) {
                this.mShareListener?.onComplete(getPlatformType())
            }

            BaseResp.ErrCode.ERR_USER_CANCEL      //分享取消
            -> if (this.mShareListener != null) {
                this.mShareListener?.onCancel(getPlatformType())
            }

            else    //分享失败
            -> {
                val err = TextUtils.concat("weixin share error (", resp.errCode.toString(), "):", resp.errStr)
                mShareListener?.onError(getPlatformType(), err.toString())
            }
        }
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }

    fun getWxApi(): IWXAPI? {
        return this.mWXApi
    }

    private fun getPlatformType(): PlatformType {
        return mConfig?.name ?: PlatformType.WEIXIN
    }


    fun getWXEventHandler(): IWXAPIEventHandler {
        return this.mEventHandler
    }
}