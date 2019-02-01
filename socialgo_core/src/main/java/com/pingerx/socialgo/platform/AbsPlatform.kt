package com.pingerx.socialgo.platform


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.pingerx.socialgo.listener.OnLoginListener
import com.pingerx.socialgo.listener.OnPayListener
import com.pingerx.socialgo.listener.OnShareListener
import com.pingerx.socialgo.model.ShareEntity
import com.pingerx.socialgo.uikit.BaseActionActivity

/**
 * 第三方平台基类
 */
abstract class AbsPlatform(protected var appId: String?, protected var appName: String?) : IPlatform {

    protected var mTarget: Int = 0

    fun setTarget(target: Int) {
        this.mTarget = target
    }

    protected open fun checkPlatformConfig(): Boolean {
        return !TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appName)
    }

    override fun getActionClazz(): Class<*> {
        return BaseActionActivity::class.java
    }

    override fun login(activity: Activity, listener: OnLoginListener) {

    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {

    }

    override fun doPay(context: Context, params: String, listener: OnPayListener) {
    }

    override fun recycle() {

    }


    //        when (entity.getShareType()) {
//            ShareEntity.SHARE_OPEN_APP -> shareOpenApp(target, activity, entity)
//            ShareEntity.SHARE_TYPE_TEXT -> shareText(target, activity, entity)
//            ShareEntity.SHARE_TYPE_IMAGE -> shareImage(target, activity, entity)
//            ShareEntity.SHARE_TYPE_APP -> shareApp(target, activity, entity)
//            ShareEntity.SHARE_TYPE_WEB -> shareWeb(target, activity, entity)
//            ShareEntity.SHARE_TYPE_MUSIC -> shareMusic(target, activity, entity)
//            ShareEntity.SHARE_TYPE_VIDEO -> shareVideo(target, activity, entity)
//        }

//    protected fun shareVideoByIntent(activity: Activity, obj: ShareEntity, pkg: String, page: String) {
//        val result = SocialGoUtils.shareVideo(activity, obj.getMediaPath(), pkg, page)
//        if (result) {
//            this.mShareListener.getFunction().onSuccess?.invoke()
//        } else {
//            this.mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareVideo by intent$pkg  $page failure"))
//        }
//    }
//
//    protected fun shareTextByIntent(activity: Activity, entity: ShareEntity, pkg: String, page: String) {
//        val result = SocialGoUtils.shareText(activity, entity.getTitle(), entity.getSummary(), pkg, page)
//        if (result) {
//            this.mShareListener.getFunction().onSuccess?.invoke()
//        } else {
//            this.mShareListener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL, "shareText by intent$pkg  $page failure"))
//        }
//    }


    ///////////////////////////////////////////////////////////////////////////
    // life circle
    ///////////////////////////////////////////////////////////////////////////

    override fun handleIntent(activity: Activity) {

    }

    override fun onResponse(resp: Any) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}
