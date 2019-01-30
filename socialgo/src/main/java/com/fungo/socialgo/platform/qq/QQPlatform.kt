package com.fungo.socialgo.platform.qq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.platform.AbsPlatform
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.PlatformCreator
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import java.util.*

/**
 * 问题汇总：com.mTencentApi.tauth.AuthActivity需要添加（ <data android:scheme="tencent110557146"></data>）否则会一直返回分享取消
 * qq空间支持本地视频分享，网络视频使用web形式分享
 * qq好友不支持本地视频分享，支持网络视频分享
 *
 *
 * 登录分享文档 http://wiki.open.qq.com/wiki/QQ%E7%94%A8%E6%88%B7%E8%83%BD%E5%8A%9B
 */
class QQPlatform constructor(context: Context, appId: String?, appName: String?) : AbsPlatform(appId, appName) {

    private var mTencentApi: Tencent = Tencent.createInstance(appId, context)
    private lateinit var mQQLoginHelper: QQLoginHelper
    private lateinit var mIUiListenerWrap: IUiListenerWrap

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform? {
            var platform: IPlatform? = null
            val config = SocialSdk.getConfig()
            if (!SocialGoUtils.isAnyEmpty(config.getQqAppId(), config.getAppName())) {
                platform = QQPlatform(context, config.getQqAppId(), config.getAppName())
            }
            return platform
        }
    }

    override fun initOnShareListener(listener: OnShareListener) {
        super.initOnShareListener(listener)
        this.mIUiListenerWrap = IUiListenerWrap(listener)
    }

    override fun recycle() {
        mTencentApi.releaseResource()
    }

    override fun isInstall(context: Context): Boolean {
        return mTencentApi.isQQInstalled(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
            Tencent.handleResultData(data, mIUiListenerWrap)
        } else if (requestCode == Constants.REQUEST_LOGIN) {
            mQQLoginHelper.handleResultData(data)
        }
    }

    override fun login(activity: Activity, listener: OnLoginListener?) {
        if (!mTencentApi.isSupportSSOLogin(activity)) {
            // 下载最新版
            listener?.onFailure(SocialError(SocialError.CODE_VERSION_LOW))
            return
        }
        mQQLoginHelper = QQLoginHelper(activity, mTencentApi, listener)
        mQQLoginHelper.login()
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

    override fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val rst = SocialGoUtils.openApp(activity, SocialConstants.QQ_PKG)
        if (rst) {
            mShareListener?.onSuccess()
        } else {
            mShareListener?.onFailure(SocialError(SocialError.CODE_CANNOT_OPEN_ERROR, "#shareOpenApp#open app error"))
        }
    }


    public override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            shareTextByIntent(activity, entity, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            val params = Bundle()
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
            mTencentApi.publishToQzone(activity, params, mIUiListenerWrap)
        }
    }

    public override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 可以兼容分享图片和gif
            val params = buildCommonBundle("", entity.getSummary(), "", shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, entity.getThumbImagePath())
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            val params = Bundle()
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
            val imageUrls = ArrayList<String>()
            imageUrls.add(entity.getThumbImagePath())
            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls)
            mTencentApi.publishToQzone(activity, params, mIUiListenerWrap)
        }
    }

    public override fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP)
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, entity)
        }
    }


    public override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 分享图文
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            // 本地或网络路径
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap)
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
            mTencentApi.shareToQzone(activity, params, mIUiListenerWrap)
        }
    }

    public override fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            val params = buildCommonBundle(entity.getTitle(), entity.getSummary(), entity.getTargetUrl(), shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO)
            if (!TextUtils.isEmpty(entity.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getThumbImagePath())
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, entity.getMediaPath())
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, entity)
        }
    }

    public override fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            when {
                SocialGoUtils.isHttpPath(entity.getMediaPath()) -> {
                    SocialLogUtils.e("qq不支持分享网络视频，使用web分享代替")
                    entity.setTargetUrl(entity.getMediaPath())
                    shareWeb(shareTarget, activity, entity)
                }
                SocialGoUtils.isExist(entity.getMediaPath()) -> shareVideoByIntent(activity, entity, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE)
                else -> this.mIUiListenerWrap.onError(SocialError(SocialError.CODE_FILE_NOT_FOUND))
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
                    mTencentApi.publishToQzone(activity, params, mIUiListenerWrap)
                }
                else -> this.mIUiListenerWrap.onError(SocialError(SocialError.CODE_FILE_NOT_FOUND))
            }
        }
    }


    private inner class IUiListenerWrap constructor(private val listener: OnShareListener) : IUiListener {

        override fun onComplete(o: Any) {
            listener.onSuccess()
        }

        override fun onError(uiError: UiError) {
            listener.onFailure(SocialError(SocialError.CODE_SDK_ERROR, "#IUiListenerWrap#分享失败 " + parseUiError(uiError)))
        }

        fun onError(e: SocialError) {
            listener.onFailure(e)
        }

        override fun onCancel() {
            listener.onCancel()
        }
    }

    override fun doPay(context: Context, params: String, listener: OnPayListener?) {

    }

    companion object {
        fun parseUiError(e: UiError): String {
            return "code = " + e.errorCode + " ,msg = " + e.errorMessage + " ,detail=" + e.errorDetail
        }
    }


}
