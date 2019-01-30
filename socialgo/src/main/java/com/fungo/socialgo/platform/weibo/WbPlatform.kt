package com.fungo.socialgo.platform.weibo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import bolts.Task
import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.common.ThumbDataContinuation
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.platform.AbsPlatform
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.PlatformCreator
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.constant.WBConstants
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler
import com.sina.weibo.sdk.utils.Utility
import java.io.File

/**
 * 新浪微博平台实现
 * 文本相同的分享不允许重复发送，会发送不出去
 * 分享支持的检测
 */
class WbPlatform constructor(context: Context, appId: String?, appName: String?, redirectUrl: String?, scope: String?) : AbsPlatform(appId, appName) {

    private var mShareHandler: WbShareHandler? = null
    private var mLoginHelper: WbLoginHelper? = null
    private var mOpenApiShareHelper: OpenApiShareHelper? = null

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform? {
            var platform: WbPlatform? = null
            val config = SocialSdk.getConfig()
            val appId = config.getSinaAppId()
            val appName = config.getAppName()
            val redirectUrl = config.getSinaRedirectUrl()
            val scope = config.getSinaScope()
            if (!SocialGoUtils.isAnyEmpty(appId, appName, redirectUrl, scope)) {
                platform = WbPlatform(context, appId, appName, redirectUrl, scope)
                platform.setTarget(target)
            }
            return platform
        }
    }

    init {
        WbSdk.install(context, AuthInfo(context, appId, redirectUrl, scope))
    }

    override fun isInstall(context: Context): Boolean {
        return if (mTarget == Target.LOGIN_WB) {
            // 支持网页授权，所以不需要安装 app
            true
        } else WbSdk.isWbInstall(context)
    }

    override fun recycle() {
        mShareHandler = null
        mLoginHelper?.recycle()
        mLoginHelper = null
        mOpenApiShareHelper = null
    }

    // 延迟获取 ShareHandler
    private fun makeWbShareHandler(activity: Activity): WbShareHandler {
        if (mShareHandler == null) {
            mShareHandler = WbShareHandler(activity)
            mShareHandler!!.registerApp()
        }
        return mShareHandler!!
    }

    // 延迟创建 login helper
    private fun makeLoginHelper(activity: Activity): WbLoginHelper {
        if (mLoginHelper == null) {
            mLoginHelper = WbLoginHelper(activity)
        }
        return mLoginHelper!!
    }

    // 延迟创建 openApi 辅助
    private fun makeOpenApiShareHelper(activity: Activity): OpenApiShareHelper {
        if (mOpenApiShareHelper == null) {
            mOpenApiShareHelper = OpenApiShareHelper(makeLoginHelper(activity), mShareListener)
        }
        return mOpenApiShareHelper!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mLoginHelper?.authorizeCallBack(requestCode, resultCode, data)
    }

    override fun handleIntent(activity: Activity) {
        if (mShareListener != null && activity is WbShareCallback && mShareHandler != null) {
            makeWbShareHandler(activity).doResultIntent(activity.intent, activity as WbShareCallback)
        }
    }

    override fun onResponse(resp: Any) {
        if (resp is Int && mShareListener != null) {
            when (resp) {
                WBConstants.ErrorCode.ERR_OK ->
                    // 分享成功
                    mShareListener?.onSuccess()
                WBConstants.ErrorCode.ERR_CANCEL ->
                    // 分享取消
                    mShareListener?.onCancel()
                WBConstants.ErrorCode.ERR_FAIL ->
                    // 分享失败
                    mShareListener?.onFailure(SocialError(SocialError.CODE_SDK_ERROR, "$TAG#微博分享失败"))
            }
        }
    }

    override fun login(activity: Activity, listener: OnLoginListener?) {
        makeLoginHelper(activity).login(activity, listener)
    }

    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val rst = SocialGoUtils.openApp(activity, SocialConstants.SINA_PKG)
        if (rst) {
            mShareListener?.onSuccess()
        } else {
            mShareListener?.onFailure(SocialError(SocialError.CODE_CANNOT_OPEN_ERROR, "open app error"))
        }
    }

    public override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val multiMessage = WeiboMultiMessage()
        multiMessage.textObject = getTextObj(entity.getSummary())
        makeWbShareHandler(activity).shareMessage(multiMessage, false)
    }

    public override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (SocialGoUtils.isGifFile(entity.getThumbImagePath())) {
            makeOpenApiShareHelper(activity).post(activity, entity)
        } else {
            SocialGoUtils.getStaticSizeBitmapByteByPathTask(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
                    .continueWith(object : ThumbDataContinuation(TAG, "shareImage", mShareListener) {
                        override fun onSuccess(thumbData: ByteArray) {
                            val multiMessage = WeiboMultiMessage()
                            multiMessage.imageObject = getImageObj(entity.getThumbImagePath(), thumbData)
                            multiMessage.textObject = getTextObj(entity.getSummary())
                            makeWbShareHandler(activity).shareMessage(multiMessage, false)
                        }
                    }, Task.UI_THREAD_EXECUTOR)
        }

    }

    public override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialLogUtils.e(TAG, "sina不支持app分享，将以web形式分享")
        shareWeb(shareTarget, activity, entity)
    }

    public override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGoUtils.getStaticSizeBitmapByteByPathTask(entity.getThumbImagePath(), AbsPlatform.THUMB_IMAGE_SIZE)
                .continueWith(object : ThumbDataContinuation(TAG, "shareWeb", mShareListener) {
                    override fun onSuccess(thumbData: ByteArray) {
                        val multiMessage = WeiboMultiMessage()
                        checkAddTextAndImageObj(multiMessage, entity, thumbData)
                        multiMessage.mediaObject = getWebObj(entity, thumbData)
                        makeWbShareHandler(activity).shareMessage(multiMessage, false)
                    }
                }, Task.UI_THREAD_EXECUTOR)
    }

    public override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        shareWeb(shareTarget, activity, entity)
    }

    public override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val mediaPath = entity.getMediaPath()
        if (SocialGoUtils.isExist(mediaPath)) {
            val multiMessage = WeiboMultiMessage()
            checkAddTextAndImageObj(multiMessage, entity, null)
            multiMessage.videoSourceObject = getVideoObj(entity)
            makeWbShareHandler(activity).shareMessage(multiMessage, false)
        } else {
            shareWeb(shareTarget, activity, entity)
        }
    }


    /**
     * 根据ShareMediaObj配置来检测是不是添加文字和照片
     */
    private fun checkAddTextAndImageObj(multiMessage: WeiboMultiMessage, obj: ShareEntity, thumbData: ByteArray?) {
        if (obj.isSinaWithPicture())
            multiMessage.imageObject = getImageObj(obj.getThumbImagePath(), thumbData)
        if (obj.isSinaWithSummary())
            multiMessage.textObject = getTextObj(obj.getSummary())
    }


    private fun getTextObj(summary: String?): TextObject {
        val textObject = TextObject()
        textObject.text = summary
        return textObject
    }


    private fun getImageObj(localPath: String?, data: ByteArray?): ImageObject {
        val imageObject = ImageObject()
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        imageObject.imageData = data
        imageObject.imagePath = localPath
        return imageObject
    }


    private fun getWebObj(obj: ShareEntity, thumbData: ByteArray): WebpageObject {
        val mediaObject = WebpageObject()
        mediaObject.identify = Utility.generateGUID()
        mediaObject.title = obj.getTitle()
        mediaObject.description = obj.getSummary()
        // 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.thumbData = thumbData
        mediaObject.actionUrl = obj.getTargetUrl()
        mediaObject.defaultText = obj.getSummary()
        return mediaObject
    }


    private fun getVideoObj(obj: ShareEntity): VideoSourceObject {
        val mediaObject = VideoSourceObject()
        mediaObject.videoPath = Uri.fromFile(File(obj.getMediaPath()))
        mediaObject.identify = Utility.generateGUID()
        mediaObject.title = obj.getTitle()
        mediaObject.description = obj.getSummary()
        // 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.actionUrl = obj.getTargetUrl()
        mediaObject.during = (if (obj.getDuration() == 0) 10 else obj.getDuration()).toLong()
        return mediaObject
    }

    companion object {
        private val TAG = WbPlatform::class.java.simpleName
    }

    override fun doPay(context: Context, params: String, listener: OnPayListener?) {

    }

}
