package com.fungo.socialgo.wechat

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.fungo.socialgo.SocialGo
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.model.LoginResult
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.platform.AbsPlatform
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.PlatformCreator
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import com.fungo.socialgo.wechat.pay.WXPay
import com.fungo.socialgo.wechat.uikit.WxActionActivity
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * 微信平台
 * [分享与收藏文档](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317340&token=&lang=zh_CN)
 * [微信登录文档](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317851&token=&lang=zh_CN)
 *
 * 缩略图不超过 32kb
 * 源文件不超过 10M
 */
class WxPlatform constructor(context: Context, appId: String?, private val wxSecret: String?, appName: String?) : AbsPlatform(appId, appName) {

    private lateinit var mWeChatLoginHelper: WxLoginHelper
    private var mWxApi: IWXAPI = WXAPIFactory.createWXAPI(context, appId, true)

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            if (SocialGoUtils.isAnyEmpty(config.getWxAppId(), config.getWxSecretKey())) {
                throw IllegalArgumentException(SocialError.MSG_WX_ID_NULL)
            }
            return WxPlatform(context, config.getWxAppId(), config.getWxSecretKey(), config.getAppName())
        }
    }

    init {
        mWxApi.registerApp(appId)
        // 支付使用
        WXPay.initWxApi(mWxApi)
    }

    override fun checkPlatformConfig(): Boolean {
        return super.checkPlatformConfig() && !TextUtils.isEmpty(wxSecret)
    }

    override fun isInstall(context: Context): Boolean {
        return mWxApi.isWXAppInstalled
    }

    override fun recycle() {
        mWxApi.detach()
    }

    override fun handleIntent(activity: Activity) {
        if (activity is IWXAPIEventHandler) {
            mWxApi.handleIntent(activity.intent, activity as IWXAPIEventHandler)
        }
    }

    override fun getActionClazz(): Class<*> {
        return WxActionActivity::class.java
    }

    override fun onResponse(resp: Any) {
        if (resp !is BaseResp) {
            return
        }
        when {
            resp.type == ConstantsAPI.COMMAND_SENDAUTH -> {
                // 登录
                val listener = mWeChatLoginHelper.getLoginListener()
                when (resp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> {
                        // 用户同意  authResp.country;  authResp.lang;  authResp.state;
                        val authResp = resp as SendAuth.Resp
                        // 这个code如果需要使用微信充值功能的话，服务端需要使用
                        // 这里为了安全暂时不提供出去
                        val authCode = authResp.code
                        if (SocialGo.getConfig().isOnlyAuthCode()) {
                            listener?.getFunction()?.onLoginSuccess?.invoke(LoginResult(Target.LOGIN_WX, authCode))
                        } else {
                            mWeChatLoginHelper.getAccessTokenByCode(authCode)
                        }
                    }
                    BaseResp.ErrCode.ERR_USER_CANCEL ->
                        // 用户取消
                        listener?.getFunction()?.onCancel?.invoke()
                    BaseResp.ErrCode.ERR_AUTH_DENIED ->
                        // 用户拒绝授权
                        listener?.getFunction()?.onCancel?.invoke()
                }
            }
            resp.type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX -> {
                // 分享
                when (resp.errCode) {
                    BaseResp.ErrCode.ERR_OK ->
                        // 分享成功
                        mShareListener.getFunction().onSuccess?.invoke()
                    BaseResp.ErrCode.ERR_USER_CANCEL ->
                        // 分享取消
                        mShareListener.getFunction().onCancel?.invoke()
                    BaseResp.ErrCode.ERR_SENT_FAILED ->
                        // 分享失败
                        mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "分享失败"))
                    BaseResp.ErrCode.ERR_AUTH_DENIED ->
                        // 分享被拒绝
                        mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "分享被拒绝"))
                }
            }
            resp.type == ConstantsAPI.COMMAND_PAY_BY_WX -> WXPay.getInstance().onResp(resp.errCode)
        }
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        mWeChatLoginHelper = WxLoginHelper(activity, mWxApi, appId)
        mWeChatLoginHelper.login(wxSecret, listener)
    }

    override fun doPay(context: Context, params: String, listener: OnPayListener) {
        // 判断微信当前版本是否支持支付
        if (mWxApi.wxAppSupportAPI < Build.PAY_SUPPORTED_SDK_INT) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_VERSION_LOW))
            return
        }
        WXPay.getInstance().doPay(params, listener)
    }

    private fun getShareToWhere(shareTarget: Int): Int {
        var where = SendMessageToWX.Req.WXSceneSession
        when (shareTarget) {
            Target.SHARE_WX_FRIENDS -> where = SendMessageToWX.Req.WXSceneSession
            Target.SHARE_WX_ZONE -> where = SendMessageToWX.Req.WXSceneTimeline
        }
        return where
    }


    private fun sendMsgToWx(msg: WXMediaMessage, shareTarget: Int, sign: String) {
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction(sign)
        req.message = msg
        req.scene = getShareToWhere(shareTarget)
        val sendResult = mWxApi.sendReq(req)
        if (!sendResult) {
            mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "$#sendMsgToWx失败，可能是参数错误"))
        }
    }

    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val rst = mWxApi.openWXApp()
        if (rst) {
            mShareListener.getFunction().onSuccess?.invoke()
        } else {
            mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_CANNOT_OPEN_ERROR))
        }
    }

    public override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val textObj = WXTextObject()
        textObj.text = entity.getSummary()
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.title = entity.getTitle()
        msg.description = entity.getSummary()
        sendMsgToWx(msg, shareTarget, "text")
    }

    public override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    getThumbImageFailure("shareImage")
                } else {
                    shareImage(shareTarget, entity.getSummary(), entity.getThumbImagePath(), thumbData)
                }
            }
        }
    }


    private fun shareImage(shareTarget: Int, desc: String, localPath: String, thumbData: ByteArray) {
        if (shareTarget == Target.SHARE_WX_FRIENDS) {
            if (SocialGoUtils.isGifFile(localPath)) {
                shareEmoji(shareTarget, localPath, desc, thumbData)
            } else {
                shareImage(shareTarget, localPath, thumbData)
            }
        } else {
            shareImage(shareTarget, localPath, thumbData)
        }
    }

    private fun shareImage(shareTarget: Int, localPath: String, thumbData: ByteArray) {
        // 文件大小不大于10485760  路径长度不大于10240
        val imgObj = WXImageObject()
        imgObj.imagePath = localPath
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        msg.thumbData = thumbData
        sendMsgToWx(msg, shareTarget, "image")
    }

    private fun shareEmoji(shareTarget: Int, localPath: String, desc: String, thumbData: ByteArray) {
        val emoji = WXEmojiObject()
        emoji.emojiPath = localPath
        val msg = WXMediaMessage()
        msg.mediaObject = emoji
        msg.description = desc
        msg.thumbData = thumbData
        sendMsgToWx(msg, shareTarget, "emoji")
    }


    public override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialLogUtils.e("微信不支持app分享，将以web形式分享")
        shareWeb(shareTarget, activity, entity)
    }

    public override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    getThumbImageFailure("shareWeb")
                } else {
                    val webPage = WXWebpageObject()
                    webPage.webpageUrl = entity.getTargetUrl()
                    val msg = WXMediaMessage(webPage)
                    msg.title = entity.getTitle()
                    msg.description = entity.getSummary()
                    msg.thumbData = thumbData
                    sendMsgToWx(msg, shareTarget, "web")
                }
            }
        }
    }


    public override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    getThumbImageFailure("shareMusic")
                } else {
                    val music = WXMusicObject()
                    music.musicUrl = entity.getMediaPath()
                    val msg = WXMediaMessage()
                    msg.mediaObject = music
                    msg.title = entity.getTitle()
                    msg.description = entity.getSummary()
                    msg.thumbData = thumbData
                    sendMsgToWx(msg, shareTarget, "music")
                }
            }
        }
    }

    public override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_WX_FRIENDS) {
            when {
                SocialGoUtils.isHttpPath(entity.getMediaPath()) -> shareWeb(shareTarget, activity, entity)
                SocialGoUtils.isExist(entity.getMediaPath()) -> shareVideoByIntent(activity, entity, SocialConstants.WECHAT_PKG, SocialConstants.WX_FRIEND_PAGE)
                else -> mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_FILE_NOT_FOUND))
            }
        } else {
            SocialGo.getExecutor().execute {
                val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
                SocialGo.getHandler().post {
                    if (thumbData == null) {
                        getThumbImageFailure("shareVideo")
                    } else {
                        val video = WXVideoObject()
                        video.videoUrl = entity.getMediaPath()
                        val msg = WXMediaMessage(video)
                        msg.title = entity.getTitle()
                        msg.description = entity.getSummary()
                        msg.thumbData = thumbData
                        sendMsgToWx(msg, shareTarget, "video")
                    }
                }
            }
        }
    }


    private fun getThumbImageFailure(msg: String) {
        SocialLogUtils.e("图片压缩失败 -> $msg")
        mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_IMAGE_COMPRESS_ERROR, msg))
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }
}
