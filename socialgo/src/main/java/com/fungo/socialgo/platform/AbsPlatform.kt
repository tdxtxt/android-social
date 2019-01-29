package com.fungo.socialgo.platform


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils

import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.model.ShareEntity
import com.fungo.socialgo.utils.SocialGoUtils

/**
 * 第三方平台基类
 */
abstract class AbsPlatform(protected var appId: String?, protected var appName: String?) : IPlatform {

    protected var mOnShareListener: OnShareListener? = null
    protected var mTarget: Int = 0

    fun setTarget(target: Int) {
        this.mTarget = target
    }

    override fun checkPlatformConfig(): Boolean {
        return !TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appName)
    }

    override fun initOnShareListener(listener: OnShareListener) {
        this.mOnShareListener = listener
    }

    override fun isInstall(context: Context): Boolean {
        return false
    }

    override fun login(activity: Activity, listener: OnLoginListener?) {

    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity) {
        when (entity.getShareType()) {
            ShareEntity.SHARE_OPEN_APP -> shareOpenApp(target, activity, entity)
            ShareEntity.SHARE_TYPE_TEXT -> shareText(target, activity, entity)
            ShareEntity.SHARE_TYPE_IMAGE -> shareImage(target, activity, entity)
            ShareEntity.SHARE_TYPE_APP -> shareApp(target, activity, entity)
            ShareEntity.SHARE_TYPE_WEB -> shareWeb(target, activity, entity)
            ShareEntity.SHARE_TYPE_MUSIC -> shareMusic(target, activity, entity)
            ShareEntity.SHARE_TYPE_VIDEO -> shareVideo(target, activity, entity)
        }
    }

    protected fun shareVideoByIntent(activity: Activity, obj: ShareEntity, pkg: String, page: String) {
        val result = SocialGoUtils.shareVideo(activity, obj.getMediaPath(), pkg, page)
        if (result) {
            this.mOnShareListener?.onSuccess()
        } else {
            this.mOnShareListener?.onFailure(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareVideo by intent$pkg  $page failure"))
        }
    }

    protected fun shareTextByIntent(activity: Activity, entity: ShareEntity, pkg: String, page: String) {
        val result = SocialGoUtils.shareText(activity, entity.getTitle(), entity.getSummary(), pkg, page)
        if (result) {
            this.mOnShareListener?.onSuccess()
        } else {
            this.mOnShareListener?.onFailure(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareText by intent$pkg  $page failure"))
        }
    }

    protected abstract fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity)

    protected abstract fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity)


    ///////////////////////////////////////////////////////////////////////////
    // life circle
    ///////////////////////////////////////////////////////////////////////////

    override fun handleIntent(activity: Activity) {

    }

    override fun onResponse(resp: Any) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    companion object {
        const val THUMB_IMAGE_SIZE = 32 * 1024
    }

}
