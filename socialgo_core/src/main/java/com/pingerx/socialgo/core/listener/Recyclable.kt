package com.pingerx.socialgo.core.listener

/**
 * 可被回收的，用来标记
 */
interface Recyclable {
    /**
     * 回收执行
     */
    fun recycle()
}
