package com.fungo.socialgo.share.media

import android.graphics.Bitmap

/**
 * @author Pinger
 * @since 18-7-20 上午10:13
 *　分享图文
 */

data class ShareTextImageMedia(var image: Bitmap, var text: String) : IShareMedia