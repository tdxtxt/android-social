package com.fungo.socialgo.share.media

import android.graphics.Bitmap

/**
 * @author Pinger
 * @since 18-7-20 上午10:12
 * 分享视频
 */

data class ShareVideoMedia(var url: String, var title: String, var description: String, var thumb: Bitmap) : IShareMedia