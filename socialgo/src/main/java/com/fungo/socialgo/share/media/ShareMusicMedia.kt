package com.fungo.socialgo.share.media

import android.graphics.Bitmap

/**
 * @author Pinger
 * @since 18-7-20 上午10:07
 * 分享音乐
 */

data class ShareMusicMedia(var musicUrl: String, var title: String, var description: String, var thumb: Bitmap) : IShareMedia