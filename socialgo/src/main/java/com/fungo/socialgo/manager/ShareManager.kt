package com.fungo.socialgo.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import bolts.Continuation
import bolts.Task
import com.fungo.socialgo.SocialSdk
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.manager.PlatformManager.KEY_ACTION_TYPE
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.model.ShareEntityChecker
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.uikit.SocialActivity
import com.fungo.socialgo.utils.SocialGoUtils
import com.fungo.socialgo.utils.SocialLogUtils
import java.lang.ref.WeakReference

/**
 * CreateAt : 2017/5/19
 * Describe : 分享管理类，使用该类进行分享操作
 *
 * @author chendong
 */
object ShareManager {

    private val TAG = ShareManager::class.java.simpleName
    private var mListener: OnShareListener? = null

    /**
     * 开始分享，供外面调用
     *
     * @param context         上下文
     * @param shareTarget     分享目标
     * @param shareObj        分享对象
     * @param onShareListener 分享监听
     */
    fun share(context: Context?, @Target.ShareTarget shareTarget: Int,
              shareObj: ShareEntity, onShareListener: OnShareListener?) {
        if (context == null) {
            return
        }
        onShareListener?.onStart(shareTarget, shareObj)
        Task.callInBackground {
            prepareImageInBackground(context, shareObj)
            var temp: ShareEntity? = null
            try {
                temp = onShareListener?.onPrepareInBackground(shareTarget, shareObj)
            } catch (e: Exception) {
                SocialLogUtils.t(e)
            }

            temp ?: shareObj
        }.continueWith(Continuation<ShareEntity, Boolean> { task ->
            if (task.isFaulted || task.result == null) {
                val exception = SocialError(SocialError.CODE_COMMON_ERROR, "onPrepareInBackground error").exception(task.error)
                onShareListener?.onFailure(exception)
                return@Continuation null
            }
            doShare(context, shareTarget, task.result, onShareListener)
            true
        }, Task.UI_THREAD_EXECUTOR).continueWith { task ->
            if (task.isFaulted) {
                val exception = SocialError(SocialError.CODE_COMMON_ERROR, "ShareManager.share() error").exception(task.error)
                onShareListener?.onFailure(exception)
            }
            true
        }
    }

    // 如果是网络图片先下载
    private fun prepareImageInBackground(context: Context, shareObj: ShareEntity) {
        val thumbImagePath = shareObj.getThumbImagePath()
        // 图片路径为网络路径，下载为本地图片
        if (!TextUtils.isEmpty(thumbImagePath) && SocialGoUtils.isHttpPath(thumbImagePath)) {
            val file = SocialSdk.getRequestAdapter().getFile(thumbImagePath)
            if (SocialGoUtils.isExist(file)) {
                shareObj.setThumbImagePath(file!!.absolutePath)
            } else if (SocialSdk.getConfig().getDefImageResId() > 0) {
                val localPath = SocialGoUtils.mapResId2LocalPath(context, SocialSdk.getConfig().getDefImageResId())
                if (SocialGoUtils.isExist(localPath)) {
                    shareObj.setThumbImagePath(localPath!!)
                }
            }
        }
    }

    /**
     *  开始分享
     */
    private fun doShare(context: Context, @Target.ShareTarget shareTarget: Int, shareObj: ShareEntity, onShareListener: OnShareListener?) {
        // 对象是否完整
        if (!ShareEntityChecker.checkShareValid(shareObj, shareTarget)) {
            onShareListener?.onFailure(SocialError(SocialError.CODE_SHARE_OBJ_VALID, ShareEntityChecker.getErrorMsg()))
            return
        }
        // 是否有存储权限，读取缩略图片需要存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            onShareListener?.onFailure(SocialError(SocialError.CODE_STORAGE_READ_ERROR))
            return
        }
        // 微博、本地、视频 需要写存储的权限
        if (shareTarget == Target.SHARE_WB
                && shareObj.shareObjType == ShareEntity.SHARE_TYPE_VIDEO
                && !SocialGoUtils.isHttpPath(shareObj.getMediaPath())
                && !SocialGoUtils.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            onShareListener?.onFailure(SocialError(SocialError.CODE_STORAGE_WRITE_ERROR))
            return
        }
        mListener = onShareListener
        val platform = PlatformManager.makePlatform(context, shareTarget)
        if (!platform.isInstall(context)) {
            onShareListener?.onFailure(SocialError(SocialError.CODE_NOT_INSTALL))
            return
        }
        val intent = Intent(context, SocialActivity::class.java)
        intent.putExtra(PlatformManager.KEY_ACTION_TYPE, PlatformManager.ACTION_TYPE_SHARE)
        intent.putExtra(PlatformManager.KEY_SHARE_MEDIA_OBJ, shareObj)
        intent.putExtra(PlatformManager.KEY_SHARE_TARGET, shareTarget)
        context.startActivity(intent)
        if (context is Activity) {
            context.overridePendingTransition(0, 0)
        }
    }

    /**
     * 激活分享
     */
    fun activeShare(activity: Activity) {
        val intent = activity.intent
        val actionType = intent?.getIntExtra(KEY_ACTION_TYPE, PlatformManager.INVALID_PARAM)
        val shareTarget = intent?.getIntExtra(PlatformManager.KEY_SHARE_TARGET, PlatformManager.INVALID_PARAM)
        val shareObj = intent?.getParcelableExtra<ShareEntity>(PlatformManager.KEY_SHARE_MEDIA_OBJ)
        if (actionType != PlatformManager.ACTION_TYPE_SHARE)
            return
        if (shareTarget == PlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "shareTargetType无效")
            return
        }
        if (shareObj == null) {
            SocialLogUtils.e(TAG, "shareObj == null")
            return
        }
        if (mListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnShareListener")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            SocialLogUtils.e(TAG, "没有获取到读存储卡的权限，这可能导致某些分享不能进行")
        }
        if (PlatformManager.getPlatform() == null)
            return
        PlatformManager.getPlatform()?.initOnShareListener(FinishShareListener(activity))
        PlatformManager.getPlatform()?.share(activity, shareTarget ?: 0, shareObj)
    }

    private class FinishShareListener(activity: Activity) : OnShareListener {

        private val mActivityRef: WeakReference<Activity> = WeakReference(activity)

        override fun onStart(shareTarget: Int, obj: ShareEntity) {
            mListener?.onStart(shareTarget, obj)
        }

        @Throws(Exception::class)
        override fun onPrepareInBackground(shareTarget: Int, obj: ShareEntity): ShareEntity? {
            return mListener?.onPrepareInBackground(shareTarget, obj)
        }

        private fun finish() {
            PlatformManager.release(mActivityRef.get())
            mListener = null
        }

        override fun onSuccess() {
            mListener?.onSuccess()
            finish()
        }


        override fun onCancel() {
            mListener?.onCancel()
            finish()
        }


        override fun onFailure(e: SocialError) {
            mListener?.onFailure(e)
            finish()
        }
    }


    /**
     * 发送短信分享
     *
     * @param context 上下文
     * @param phone   手机号
     * @param msg     内容
     */
    fun sendSms(context: Context, phone: String, msg: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("smsto:$phone")
        intent.putExtra("sms_body", msg)
        intent.type = "vnd.android-dir/mms-sms"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 发送邮件分享
     *
     * @param context 上下文
     * @param mailto  email
     * @param subject 主题
     * @param msg     内容
     */
    fun sendEmail(context: Context, mailto: String, subject: String, msg: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$mailto")
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 打开某个平台app
     */
    fun openApp(context: Context, target: Int): Boolean {
        val platform = Target.mapPlatform(target)
        var pkgName: String? = null
        when (platform) {
            Target.SHARE_QQ_FRIENDS, Target.SHARE_QQ_ZONE -> pkgName = SocialConstants.QQ_PKG
            Target.SHARE_WX_FRIENDS, Target.SHARE_WX_ZONE -> pkgName = SocialConstants.WECHAT_PKG
            Target.SHARE_WB -> pkgName = SocialConstants.SINA_PKG
        }
        return !TextUtils.isEmpty(pkgName) && SocialGoUtils.openApp(context, pkgName)
    }
}
