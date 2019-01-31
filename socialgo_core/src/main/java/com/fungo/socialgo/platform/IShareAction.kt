package com.fungo.socialgo.platform

import android.app.Activity
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.model.ShareEntity

/**
 * @author Pinger
 * @since 2019/1/31 15:56
 * 分享类型
 */
interface IShareAction {

    fun shareOpenApp(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareApp(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareMusic(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareVideo(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
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
}