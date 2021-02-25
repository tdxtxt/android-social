package com.pingerx.socialgo.core.platform

import android.app.Activity
import com.pingerx.socialgo.core.listener.OnShareListener
import com.pingerx.socialgo.core.model.ShareEntity

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

    fun shareMiniProgram(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        when (entity.getShareType()) {
            ShareEntity.SHARE_OPEN_APP -> shareOpenApp(target, activity, entity)
            ShareEntity.SHARE_TYPE_TEXT -> shareText(target, activity, entity)
            ShareEntity.SHARE_TYPE_IMAGE -> shareImage(target, activity, entity)
            ShareEntity.SHARE_TYPE_APP -> shareApp(target, activity, entity)
            ShareEntity.SHARE_TYPE_WEB -> shareWeb(target, activity, entity)
            ShareEntity.SHARE_TYPE_MUSIC -> shareMusic(target, activity, entity)
            ShareEntity.SHARE_TYPE_VIDEO -> shareVideo(target, activity, entity)
            ShareEntity.SHARE_TYPE_MINIPROGRAM -> shareMiniProgram(target, activity, entity)
        }
    }
}