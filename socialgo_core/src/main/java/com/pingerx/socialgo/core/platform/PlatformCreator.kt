package com.pingerx.socialgo.core.platform

import android.content.Context

/**
 * 构建一个平台
 */
interface PlatformCreator {

    fun create(context: Context, target: Int): IPlatform?
}
