package com.fungo.socialgo.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * 分享实体内容
 * 包括文字分享，图片分享，图文分享，链接分享和其他各种分享
 */

class ShareEntity(val shareObjType: Int) : Parcelable {

    // title 标题，如果不设置为app name
    private var title: String? = null
    // 概要，描述，desc
    private var summary: String? = null
    // 缩略图地址，必传
    private var thumbImagePath: String? = null
    private var thumbImagePathNet: String? = null
    // 启动url，点击之后指向的url，启动新的网页
    private var targetUrl: String? = null
    // 资源url,音视频播放源
    private var mediaPath: String? = null
    // 音视频时间
    private var duration = 10
    // 新浪分享带不带文字
    private var isSinaWithSummary = true
    // 新浪分享带不带图片
    private var isSinaWithPicture = false
    // 使用本地 intent 打开，分享本地视频用
    private var isShareByIntent = false

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        title = parcel.readString()
        summary = parcel.readString()
        thumbImagePath = parcel.readString()
        thumbImagePathNet = parcel.readString()
        targetUrl = parcel.readString()
        mediaPath = parcel.readString()
        duration = parcel.readInt()
        isSinaWithSummary = parcel.readByte() != 0.toByte()
        isSinaWithPicture = parcel.readByte() != 0.toByte()
        isShareByIntent = parcel.readByte() != 0.toByte()
    }


    fun init(title: String, summary: String, thumbImagePath: String, targetUrl: String) {
        this.title = title
        this.summary = summary
        setThumbImagePath(thumbImagePath)
        setTargetUrl(targetUrl)
    }

    fun getTitle(): String {
        return title ?: ""
    }

    fun getSummary(): String {
        return summary ?: ""
    }

    fun getThumbImagePath(): String {
        return thumbImagePath + ""
    }

    fun setThumbImagePath(thumbImagePath: String) {
        this.thumbImagePath = thumbImagePath
        this.thumbImagePathNet = thumbImagePath
    }

    fun getTargetUrl(): String {
        return if (TextUtils.isEmpty(targetUrl)) {
            mediaPath + ""
        } else targetUrl + ""
    }

    fun setTargetUrl(targetUrl: String) {
        this.targetUrl = targetUrl
    }

    fun getMediaPath(): String {
        return if (TextUtils.isEmpty(mediaPath)) {
            targetUrl + ""
        } else mediaPath + ""
    }

    fun setMediaPath(mediaPath: String) {
        this.mediaPath = mediaPath
    }

    fun getShareType(): Int {
        return shareObjType
    }

    fun getDuration(): Int {
        return duration
    }

    fun isSinaWithPicture(): Boolean {
        return isSinaWithPicture
    }

    fun isSinaWithSummary(): Boolean {
        return isSinaWithSummary
    }

    override fun toString(): String {
        return "ShareEntity{" +
                "shareObjType=" + shareObjType +
                ", title='" + title + '\''.toString() +
                ", summary='" + summary + '\''.toString() +
                ", thumbImagePath='" + thumbImagePath + '\''.toString() +
                ", thumbImagePathNet='" + thumbImagePathNet + '\''.toString() +
                ", targetUrl='" + targetUrl + '\''.toString() +
                ", mediaPath='" + mediaPath + '\''.toString() +
                ", duration=" + duration +
                ", isSinaWithSummary=" + isSinaWithSummary +
                ", isSinaWithPicture=" + isSinaWithPicture +
                ", isShareByIntent=" + isShareByIntent +
                '}'.toString()
    }

    companion object CREATOR : Parcelable.Creator<ShareEntity> {

        const val SHARE_TYPE_TEXT = 0x41   // 分享文字
        const val SHARE_TYPE_IMAGE = 0x42  // 分享图片
        const val SHARE_TYPE_APP = 0x43    // 分享app
        const val SHARE_TYPE_WEB = 0x44    // 分享web
        const val SHARE_TYPE_MUSIC = 0x45  // 分享音乐
        const val SHARE_TYPE_VIDEO = 0x46  // 分享视频
        const val SHARE_OPEN_APP = 0x99    // 打开 app

        // 直接打开对应app
        fun buildOpenAppObj(): ShareEntity {
            return ShareEntity(SHARE_OPEN_APP)
        }

        // 分享文字，qq 好友原本不支持，使用intent兼容
        fun buildTextObj(title: String, summary: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_TEXT)
            shareMediaObj.title = title
            shareMediaObj.summary = summary
            return shareMediaObj
        }

        // 分享图片
        fun buildImageObj(path: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_IMAGE)
            shareMediaObj.setThumbImagePath(path)
            return shareMediaObj
        }

        // 分享图片，带描述，qq微信好友会分为两条消息发送
        fun buildImageObj(path: String, summary: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_IMAGE)
            shareMediaObj.setThumbImagePath(path)
            shareMediaObj.summary = summary
            return shareMediaObj
        }

        // 应用分享，qq支持，其他平台使用 web 分享兼容
        fun buildAppObj(title: String, summary: String, thumbImagePath: String, targetUrl: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_APP)
            shareMediaObj.init(title, summary, thumbImagePath, targetUrl)
            return shareMediaObj
        }

        // 分享web，打开链接
        fun buildWebObj(title: String, summary: String, thumbImagePath: String, targetUrl: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_WEB)
            shareMediaObj.init(title, summary, thumbImagePath, targetUrl)
            return shareMediaObj
        }

        // 分享音乐,qq空间不支持，使用web分享
        fun buildMusicObj(title: String, summary: String, thumbImagePath: String, targetUrl: String, mediaPath: String, duration: Int): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_MUSIC)
            shareMediaObj.init(title, summary, thumbImagePath, targetUrl)
            shareMediaObj.setMediaPath(mediaPath)
            shareMediaObj.duration = duration
            return shareMediaObj
        }

        // 分享视频，
        // 本地视频使用 intent 兼容，qq 空间本身支持本地视频发布
        // 支持网络视频
        fun buildVideoObj(title: String, summary: String, thumbImagePath: String, targetUrl: String, mediaPath: String, duration: Int): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_VIDEO)
            shareMediaObj.init(title, summary, thumbImagePath, targetUrl)
            shareMediaObj.setMediaPath(mediaPath)
            shareMediaObj.duration = duration
            return shareMediaObj
        }

        // 本地视频
        fun buildVideoObj(title: String, summary: String, localVideoPath: String): ShareEntity {
            val shareMediaObj = ShareEntity(SHARE_TYPE_VIDEO)
            shareMediaObj.setMediaPath(localVideoPath)
            shareMediaObj.isShareByIntent = true
            shareMediaObj.title = title
            shareMediaObj.summary = summary
            return shareMediaObj
        }

        override fun createFromParcel(parcel: Parcel): ShareEntity {
            return ShareEntity(parcel)
        }

        override fun newArray(size: Int): Array<ShareEntity?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(shareObjType)
        parcel.writeString(title)
        parcel.writeString(summary)
        parcel.writeString(thumbImagePath)
        parcel.writeString(thumbImagePathNet)
        parcel.writeString(targetUrl)
        parcel.writeString(mediaPath)
        parcel.writeInt(duration)
        parcel.writeByte(if (isSinaWithSummary) 1 else 0)
        parcel.writeByte(if (isSinaWithPicture) 1 else 0)
        parcel.writeByte(if (isShareByIntent) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }


}
