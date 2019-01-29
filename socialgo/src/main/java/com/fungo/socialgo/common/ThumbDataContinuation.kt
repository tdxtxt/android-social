package com.fungo.socialgo.common

import bolts.Continuation
import bolts.Task
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.utils.SocialLogUtils

/**
 * 压缩图片之后的返回结果
 */
abstract class ThumbDataContinuation protected constructor(private val tag: String, private val msg: String, private val onShareListener: OnShareListener) : Continuation<ByteArray, Any> {

    @Throws(Exception::class)
    override fun then(task: Task<ByteArray>): Any? {
        if (task.isFaulted || task.result == null) {
            SocialLogUtils.e(tag, "图片压缩失败 -> $msg")
            onShareListener.onFailure(SocialError(SocialError.CODE_IMAGE_COMPRESS_ERROR, msg).exception(task.error))
        } else {
            onSuccess(task.result)
        }
        return null
    }

    abstract fun onSuccess(thumbData: ByteArray)
}