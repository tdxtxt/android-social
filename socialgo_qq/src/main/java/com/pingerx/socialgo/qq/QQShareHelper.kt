package com.pingerx.socialgo.qq

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import com.pingerx.socialgo.common.SocialConstants
import com.pingerx.socialgo.exception.SocialError
import com.pingerx.socialgo.listener.OnShareListener
import com.pingerx.socialgo.model.ShareEntity
import com.pingerx.socialgo.platform.IShareAction
import com.pingerx.socialgo.platform.Target
import com.pingerx.socialgo.utils.SocialGoUtils
import com.pingerx.socialgo.utils.SocialLogUtils
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import java.util.*

/**
 * @author Pinger
 * @since 2019/1/31 18:19
 */
class QQShareHelper(private val tencentApi: Tencent, private val appName: String?) : IShareAction {

    private var mListener: OnShareListener? = null
    private var mIUiListenerWrap: IUiListenerWrap? = null

    fun getUIListener(): IUiListenerWrap? {
        return mIUiListenerWrap
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        mListener = listener
        if (mIUiListenerWrap == null) {
            mIUiListenerWrap = IUiListenerWrap(listener)
        }
        super.share(activity, target, entity, listener)
    }

    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val rst = SocialGoUtils.openApp(activity, SocialConstants.QQ_PKG)
        if (rst) {
            onSuccess()
        } else {
            onFailure(SocialError(SocialError.CODE_CANNOT_OPEN_ERROR, "#shareOpenApp#open app error"))
        }
    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            shareTextByIntent(activity, entity, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            val params = Bundle()
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
            tencentApi.publishToQzone(activity, params, mIUiListenerWrap)
        }
    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 可以兼容分享图片和gif
            val params = buildCommonBundle("", entity.getSummary(), "", shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, entity.getThumbImagePath())
            tencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            val params = Bundle()
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
            val imageUrls = ArrayList<String>()
            imageUrls.add(entity.getThumbImagePath())
            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls)
            tencentApi.publishToQzone(activity, params, mIUiListenerWrap)
        }
    }

    override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP)
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            tencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, entity)
        }
    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 分享图文
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            // 本地或网络路径
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            tencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else {
            val imageUrls = ArrayList<String>()
            val params = Bundle()
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
            params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME, appName)
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, entity.getTitle())
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, entity.getTargetUrl())
            imageUrls.add(entity.getThumbImagePath())
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls)
            tencentApi.shareToQzone(activity, params, mIUiListenerWrap)
        }
    }

    override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO)
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, entity.getMediaPath())
            tencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, entity)
        }
    }

    override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            when {
                SocialGoUtils.isHttpPath(entity.getMediaPath()) -> {
                    SocialLogUtils.e("qq不支持分享网络视频，使用web分享代替")
                    entity.setTargetUrl(entity.getMediaPath())
                    shareWeb(shareTarget, activity, entity)
                }
                SocialGoUtils.isExist(entity.getMediaPath()) -> shareVideoByIntent(activity, entity, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE)
                else -> getUIListener()?.onError(SocialError(SocialError.CODE_FILE_NOT_FOUND))
            }
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            // qq 空间支持本地文件发布
            when {
                SocialGoUtils.isHttpPath(entity.getMediaPath()) -> {
                    SocialLogUtils.e("qq空间网络视频，使用web形式分享")
                    shareWeb(shareTarget, activity, entity)
                }
                SocialGoUtils.isExist(entity.getMediaPath()) -> {
                    SocialLogUtils.e("qq空间本地视频分享")
                    val params = Bundle()
                    params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO)
                    params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, entity.getMediaPath())
                    tencentApi.publishToQzone(activity, params, mIUiListenerWrap)
                }
                else -> getUIListener()?.onError(SocialError(SocialError.CODE_FILE_NOT_FOUND))
            }
        }
    }

    private fun shareVideoByIntent(activity: Activity, obj: ShareEntity, pkg: String, page: String) {
        val result = SocialGoUtils.shareVideo(activity, obj.getMediaPath(), pkg, page)
        if (result) {
            onSuccess()
        } else {
            onFailure(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareVideo by intent$pkg  $page failure"))
        }
    }

    private fun shareTextByIntent(activity: Activity, entity: ShareEntity, pkg: String, page: String) {
        val result = SocialGoUtils.shareText(activity, entity.getTitle(), entity.getSummary(), pkg, page)
        if (result) {
            onSuccess()
        } else {
            onFailure(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareText by intent$pkg  $page failure"))
        }
    }

    private fun buildCommonBundle(title: String, summary: String, targetUrl: String?, shareTarget: Int): Bundle {
        val params = Bundle()
        if (!TextUtils.isEmpty(title))
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
        if (!TextUtils.isEmpty(summary))
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary)
        if (!TextUtils.isEmpty(targetUrl))
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl)
        if (!TextUtils.isEmpty(appName))
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName)
        // 加了这个会自动打开qq空间发布
        if (shareTarget == Target.SHARE_QQ_ZONE)
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN)
        return params
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

    inner class IUiListenerWrap constructor(private val listener: OnShareListener) : IUiListener {
        override fun onComplete(o: Any) {
            listener.getFunction().onSuccess?.invoke()
        }

        override fun onError(uiError: UiError) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SDK_ERROR, "#IUiListenerWrap#分享失败 " + parseUiError(uiError)))
        }

        fun onError(e: SocialError) {
            listener.getFunction().onFailure?.invoke(e)
        }

        override fun onCancel() {
            listener.getFunction().onCancel?.invoke()
        }
    }

    private fun parseUiError(e: UiError): String {
        return "code = " + e.errorCode + " ,msg = " + e.errorMessage + " ,detail=" + e.errorDetail
    }
}