package com.fungo.socialgo.share.media

import android.graphics.Bitmap

/**
 * @author Pinger
 * @since 18-7-20 上午10:10
 * 分享网页链接
 */

data class ShareWebMedia(var url: String, var title: String, var description: String, var thumb: Bitmap) : IShareMedia