package com.fungo.socialgo.share.config

import java.util.*

/**
 * @author Pinger
 * @since 18-7-20 上午9:45
 * 平台配置Key的必须的属性
 */

object PlatformConfig {

    var configs: MutableMap<PlatformType, Platform> = HashMap()

    interface Platform {
        val name: PlatformType
    }

    init {
        configs[PlatformType.WEIXIN] = Weixin(PlatformType.WEIXIN)
        configs[PlatformType.WEIXIN_CIRCLE] = Weixin(PlatformType.WEIXIN_CIRCLE)
        configs[PlatformType.QQ] = QQ(PlatformType.QQ)
        configs[PlatformType.QZONE] = QQ(PlatformType.QZONE)
        configs[PlatformType.SINA_WB] = SinaWB(PlatformType.SINA_WB)
    }


    // 微信
    class Weixin(private val media: PlatformType, override val name: PlatformType = media) : Platform {
        var appId: String? = null
    }


    //qq
    class QQ(private val media: PlatformType, override val name: PlatformType = media) : Platform {
        var appId: String? = null
    }


    //weibo
    class SinaWB(private val media: PlatformType, override val name: PlatformType = media) : Platform {
        var appKey: String? = null
    }


    /**
     * 设置微信配置信息
     * @param appId
     */
    fun setWeixin(appId: String) {
        // 微信
        (configs[PlatformType.WEIXIN] as Weixin).appId = appId

        // 朋友圈
        (configs[PlatformType.WEIXIN_CIRCLE] as Weixin).appId = appId
    }


    /**
     * 设置qq配置信息
     * @param appId
     */
    fun setQQ(appId: String) {
        // QQ
        (configs[PlatformType.QQ] as QQ).appId = appId
        // QQ空间
        (configs[PlatformType.QZONE] as QQ).appId = appId
    }


    /**
     * 设置新浪微博配置信息
     * @param appKey
     */
    fun setSinaWB(appKey: String) {
        (configs[PlatformType.SINA_WB] as SinaWB).appKey = appKey
    }


    /**
     * 获取设置的平台的配置
     */
    fun getPlatformConfig(platformType: PlatformType): Platform {
        return configs[platformType]!!
    }


}