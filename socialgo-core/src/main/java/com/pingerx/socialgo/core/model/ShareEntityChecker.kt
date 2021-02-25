package com.pingerx.socialgo.core.model


import com.pingerx.socialgo.core.platform.Target
import com.pingerx.socialgo.core.utils.SocialGoUtils

import java.lang.ref.WeakReference

/**
 * 检查分享对象的工具类
 */
object ShareEntityChecker {

    private var sErrMsgRef = ErrMsgRef("", null)

    class ErrMsgRef(msg: String, obj: ShareEntity?) : WeakReference<String>("$msg data = " + (obj?.toString()
            ?: "no data"))

    fun getErrorMsg(): String? {
        return sErrMsgRef.get()
    }

    fun checkShareValid(entity: ShareEntity, shareTarget: Int): Boolean {
        when (entity.getShareType()) {
            // 小程序分享
            ShareEntity.SHARE_TYPE_MINIPROGRAM -> {
                return isMinProgramValid(entity)
            }
            // 文字分享，title,summary 必须有
            ShareEntity.SHARE_TYPE_TEXT -> {
                return isTitleSummaryValid(entity)
            }
            // 图片分享，如果是微博的 open api 必须有summary
            ShareEntity.SHARE_TYPE_IMAGE -> {
                return isThumbLocalPathValid(entity)
            }
            // app 和 web
            ShareEntity.SHARE_TYPE_APP, ShareEntity.SHARE_TYPE_WEB -> {
                return (isUrlValid(entity)
                        && isTitleSummaryValid(entity)
                        && isThumbLocalPathValid(entity))
            }
            //  music voice
            ShareEntity.SHARE_TYPE_MUSIC -> {
                return isMusicVideoVoiceValid(entity) && isNetMedia(entity)
            }
            // video
            ShareEntity.SHARE_TYPE_VIDEO -> {
                // 本地视频分享，qq空间、微博自己支持，qq好友、微信好友、钉钉 使用 intent 支持
                return if (SocialGoUtils.isExist(entity.getMediaPath()) && isAny(shareTarget, Target.SHARE_QQ_ZONE,
                                Target.SHARE_WB,
                                Target.SHARE_QQ_FRIENDS,
                                Target.SHARE_WX_FRIENDS)) {
                    isTitleSummaryValid(entity) && !SocialGoUtils.isAnyEmpty(entity.getMediaPath())
                } else if (SocialGoUtils.isHttpPath(entity.getMediaPath())) {
                    // 网络视频
                    isUrlValid(entity) && isMusicVideoVoiceValid(entity) && isNetMedia(entity)
                } else {
                    sErrMsgRef = ErrMsgRef("本地不支持或者，不是本地也不是网络 ", entity)
                    false
                }
            }
            else -> return false
        }
    }

    private fun isAny(shareTarget: Int, vararg targets: Int): Boolean {
        for (target in targets) {
            if (target == shareTarget) {
                return true
            }
        }
        return false
    }

    private fun isMinProgramValid(entity: ShareEntity): Boolean {
        val valid = !SocialGoUtils.isAnyEmpty(entity.getTitle(), entity.getSummary(), entity.getTargetUrl())
        if(!valid) {
            sErrMsgRef = ErrMsgRef("title summary targetUrl不能空", entity)
        }
        return valid
    }

    private fun isTitleSummaryValid(entity: ShareEntity): Boolean {
        val valid = !SocialGoUtils.isAnyEmpty(entity.getTitle(), entity.getSummary())
        if (!valid) {
            sErrMsgRef = ErrMsgRef("title summary 不能空", entity)
        }
        return valid
    }

    // 是否是网络视频
    private fun isNetMedia(entity: ShareEntity): Boolean {
        val httpPath = SocialGoUtils.isHttpPath(entity.getMediaPath())
        if (!httpPath) {
            sErrMsgRef = ErrMsgRef("ShareEntity mediaPath 需要 网络路径", entity)
        }
        return httpPath
    }

    // url 合法
    private fun isUrlValid(entity: ShareEntity): Boolean {
        val targetUrl = entity.getTargetUrl()
        val urlValid = !SocialGoUtils.isAnyEmpty(targetUrl) && SocialGoUtils.isHttpPath(targetUrl)
        if (!urlValid) {
            sErrMsgRef = ErrMsgRef("url : $targetUrl  不能为空，且必须带有http协议头", entity)
        }
        return urlValid
    }

    // 音频视频
    private fun isMusicVideoVoiceValid(obj: ShareEntity): Boolean {
        return isTitleSummaryValid(obj) && !SocialGoUtils.isAnyEmpty(obj.getMediaPath()) && isThumbLocalPathValid(obj)
    }

    // 本地文件存在
    private fun isThumbLocalPathValid(obj: ShareEntity): Boolean {
        val thumbImagePath = obj.getThumbImagePath()
        val exist = SocialGoUtils.isExist(thumbImagePath)
        val picFile = SocialGoUtils.isPicFile(thumbImagePath)
        if (!exist || !picFile) {
            sErrMsgRef = ErrMsgRef("path : " + thumbImagePath + "  " + (if (exist) "" else "文件不存在") + if (picFile) "" else "不是图片文件", obj)
        }
        return exist && picFile
    }
}
