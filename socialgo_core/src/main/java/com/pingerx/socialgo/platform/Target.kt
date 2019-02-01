package com.pingerx.socialgo.platform

import androidx.annotation.IntDef

/**
 * 第三方平台操作的目标
 * 包括平台目标，分享目标，登录目标，分享类型，支付类型等等
 */
object Target {

    const val PLATFORM_QQ = 0x11 // qq
    const val PLATFORM_WX = 0x12 // 微信
    const val PLATFORM_WB = 0x13 // 微博
    const val PLATFORM_ALI = 0x14 // 支付宝

    const val LOGIN_QQ = 0x21 // qq 登录
    const val LOGIN_WX = 0x22 // 微信登录
    const val LOGIN_WB = 0x23 // 微博登录

    const val SHARE_QQ_FRIENDS = 0x31 // qq好友
    const val SHARE_QQ_ZONE = 0x32 // qq空间
    const val SHARE_WX_FRIENDS = 0x33 // 微信好友
    const val SHARE_WX_ZONE = 0x34 // 微信朋友圈
    const val SHARE_WB = 0x36 // 新浪微博

    const val PAY_WX = 0x41   //微信支付
    const val PAY_ALI = 0x42  // 阿里支付

    /**
     * 分享类型
     */
    @IntDef(SHARE_QQ_FRIENDS, SHARE_QQ_ZONE, SHARE_WX_FRIENDS, SHARE_WX_ZONE, SHARE_WB)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ShareTarget

    /**
     * 登录类型
     */
    @IntDef(LOGIN_QQ, LOGIN_WB, LOGIN_WX)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LoginTarget

    /**
     * 支付类型
     */
    @IntDef(PAY_WX, PAY_ALI)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PayTarget

    /**
     * 平台类型
     */
    @IntDef(PLATFORM_WX, PLATFORM_QQ, PLATFORM_WB, PLATFORM_ALI)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PlatformTarget

    fun mapPlatform(target: Int): Int {
        return when (target) {
            PLATFORM_QQ, LOGIN_QQ, SHARE_QQ_FRIENDS, SHARE_QQ_ZONE -> PLATFORM_QQ
            PLATFORM_WX, LOGIN_WX, SHARE_WX_FRIENDS, SHARE_WX_ZONE, PAY_WX -> PLATFORM_WX
            LOGIN_WB, SHARE_WB, PLATFORM_WB -> PLATFORM_WB
            PAY_ALI, PLATFORM_ALI -> PLATFORM_ALI
            else -> -1
        }
    }

    fun toDesc(target: Int): String {
        return when (target) {
            LOGIN_QQ -> "qq登录"
            LOGIN_WX -> "微信登录"
            LOGIN_WB -> "微博登录"
            SHARE_QQ_FRIENDS -> "qq好友分享"
            SHARE_QQ_ZONE -> "qq空间分享"
            SHARE_WX_FRIENDS -> "微信好友分享"
            SHARE_WX_ZONE -> "微信空间分享"
            SHARE_WB -> "微博普通分享"
            else -> "未知类型"
        }
    }
}
