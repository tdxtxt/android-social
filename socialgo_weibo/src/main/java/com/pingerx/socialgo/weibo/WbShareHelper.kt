package com.pingerx.socialgo.weibo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.pingerx.socialgo.SocialGo
import com.pingerx.socialgo.common.SocialConstants
import com.pingerx.socialgo.exception.SocialError
import com.pingerx.socialgo.listener.OnShareListener
import com.pingerx.socialgo.listener.Recyclable
import com.pingerx.socialgo.model.ShareEntity
import com.pingerx.socialgo.platform.IShareAction
import com.pingerx.socialgo.utils.SocialGoUtils
import com.pingerx.socialgo.utils.SocialLogUtils
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler
import com.sina.weibo.sdk.utils.Utility
import java.io.File

/**
 * @author Pinger
 * @since 2019/1/31 15:59
 */
class WbShareHelper : IShareAction, Recyclable {

    private var mShareHandler: WbShareHandler? = null
    private var mOpenApiShareHelper: OpenApiShareHelper? = null
    private var mListener: OnShareListener? = null

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        mListener = listener
        super.share(activity, target, entity, listener)
    }

    private fun makeWbShareHandler(activity: Activity): WbShareHandler {
        if (mShareHandler == null) {
            mShareHandler = WbShareHandler(activity)
            mShareHandler!!.registerApp()
        }
        return mShareHandler!!
    }

    private fun makeOpenApiShareHelper(activity: Activity): OpenApiShareHelper {
        if (mOpenApiShareHelper == null) {
            mOpenApiShareHelper = OpenApiShareHelper(WbLoginHelper(activity), mListener)
        }
        return mOpenApiShareHelper!!
    }


    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val rst = SocialGoUtils.openApp(activity, SocialConstants.SINA_PKG)
        if (rst) {
            onSuccess()
        } else {
            onFailure(SocialError(SocialError.CODE_CANNOT_OPEN_ERROR, "open app error"))
        }
    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val multiMessage = WeiboMultiMessage()
        multiMessage.textObject = getTextObj(entity.getSummary())
        makeWbShareHandler(activity).shareMessage(multiMessage, false)
    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (SocialGoUtils.isGifFile(entity.getThumbImagePath())) {
            makeOpenApiShareHelper(activity).post(activity, entity)
        } else {
            SocialGo.getExecutor().execute {
                val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath())
                SocialGo.getHandler().post {
                    if (thumbData == null) {
                        getThumbImageFailure("shareImage")
                    } else {
                        val multiMessage = WeiboMultiMessage()
                        multiMessage.imageObject = getImageObj(entity.getThumbImagePath(), thumbData)
                        multiMessage.textObject = getTextObj(entity.getSummary())
                        makeWbShareHandler(activity).shareMessage(multiMessage, false)
                    }
                }
            }
        }
    }

    override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialLogUtils.e("sina不支持app分享，将以web形式分享")
        shareWeb(shareTarget, activity, entity)
    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getThumbImagePath())
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    getThumbImageFailure("shareWeb")
                } else {
                    val multiMessage = WeiboMultiMessage()
                    checkAddTextAndImageObj(multiMessage, entity, thumbData)
                    multiMessage.mediaObject = getWebObj(entity, thumbData)
                    makeWbShareHandler(activity).shareMessage(multiMessage, false)
                }
            }
        }
    }

    override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        shareWeb(shareTarget, activity, entity)
    }

    override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {
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


    private fun getThumbImageFailure(msg: String) {
        SocialLogUtils.e("图片压缩失败 -> $msg")
        onFailure(SocialError(SocialError.CODE_IMAGE_COMPRESS_ERROR, msg))
    }

    fun doResultIntent(intent: Intent, callback: WbShareCallback) {
        mShareHandler?.doResultIntent(intent, callback)
    }

    fun onSuccess() {
        mListener?.getFunction()?.onSuccess?.invoke()
    }

    fun onCancel() {
        mListener?.getFunction()?.onCancel?.invoke()
    }

    fun onFailure(error: SocialError) {
        mListener?.getFunction()?.onFailure?.invoke(error)
    }

    override fun recycle() {
        mListener = null
        mOpenApiShareHelper = null
        mShareHandler = null
    }

}