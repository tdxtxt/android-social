package com.pingerx.socialgo.core.exception

import android.text.TextUtils

import com.pingerx.socialgo.core.utils.SocialLogUtils

/**
 * 登录分享回调异常信息，包括错误码和错误描述
 */
class SocialError : Exception {

    var errorCode = CODE_OK
    var errorMsg: String? = null
    var mException: Exception? = null

    constructor(errorCode: Int) {
        this.errorCode = errorCode
        when (errorCode) {
            CODE_NOT_INSTALL -> append("应用未安装")
            CODE_VERSION_LOW -> append("应用版本低,需要更高版本")
            CODE_STORAGE_READ_ERROR -> append("没有获取到读SD卡的权限，这会导致图片缩略图无法获取")
            CODE_STORAGE_WRITE_ERROR -> append("没有获取到写SD卡的权限，这会微博分享本地视频无法使用")
            CODE_SDK_ERROR -> append("SDK 返回的错误信息")
            CODE_COMMON_ERROR -> append("通用其他错误")
            CODE_SHARE_OBJ_VALID -> append("分享的对象数据有问题")
            CODE_SHARE_BY_INTENT_FAIL -> append("使用 intent 分享失败")
            CODE_FILE_NOT_FOUND -> append("没有找到文件")
            CODE_REQUEST_ERROR -> append("网络请求错误")
            CODE_CANNOT_OPEN_ERROR -> append("app 无法唤醒")
            CODE_PARSE_ERROR -> append("数据解析错误")
            CODE_IMAGE_COMPRESS_ERROR -> append("图片压缩错误")
            CODE_PAY_PARAM_ERROR -> append("支付参数错误")
            CODE_PAY_ERROR -> append("支付失败")
            CODE_PAY_RESULT_ERROR -> append("支付结果解析错误")
            CODE_DATA_EMPTY -> append("返回结果为空")
        }
    }

    constructor(errCode: Int, message: String?) {
        this.errorMsg = message
        this.errorCode = errCode
    }

    constructor(errorCode: Int, exception: Exception) {
        this.errorCode = errorCode
        mException = exception
    }

    fun exception(ex: Exception): SocialError {
        this.mException = ex
        return this
    }

    override fun printStackTrace() {
        SocialLogUtils.e(TAG, toString())
    }

    override fun toString(): String {
        val sb = StringBuilder()
                .append("errCode = ").append(errorCode)
                .append(", errMsg = ").append(errorMsg).append("\n")
        val exception = mException
        if (exception != null) {
            sb.append("其他错误 : ").append(exception.message)
            exception.printStackTrace()
        }
        return sb.toString()
    }

    fun append(msg: String): SocialError {
        if (!TextUtils.isEmpty(errorMsg)) {
            this.errorMsg = errorMsg.toString() + " ， " + msg
        } else {
            this.errorMsg = msg
        }
        return this
    }

    companion object {
        private val TAG = SocialError::class.java.simpleName

        const val CODE_OK = 1 // 成功
        const val CODE_COMMON_ERROR = 101 // 通用错误，未归类
        const val CODE_NOT_INSTALL = 102 // 没有安装应用
        const val CODE_VERSION_LOW = 103 // 版本过低，不支持
        const val CODE_SHARE_OBJ_VALID = 104 // 分享的对象参数有问题
        const val CODE_SHARE_BY_INTENT_FAIL = 105 // 使用 Intent 分享失败
        const val CODE_STORAGE_READ_ERROR = 106 // 没有读存储的权限，获取分享缩略图将会失败
        const val CODE_STORAGE_WRITE_ERROR = 107 // 没有写存储的权限，微博分享视频copy操作将会失败
        const val CODE_FILE_NOT_FOUND = 108 // 文件不存在
        const val CODE_SDK_ERROR = 109 // sdk 返回错误
        const val CODE_REQUEST_ERROR = 110 // 网络请求发生错误
        const val CODE_CANNOT_OPEN_ERROR = 111 // 无法启动 app
        const val CODE_PARSE_ERROR = 112 // 数据解析错误
        const val CODE_IMAGE_COMPRESS_ERROR = 113 // 图片压缩失败
        const val CODE_PAY_PARAM_ERROR = 114 // 支付参数错误
        const val CODE_PAY_ERROR = 115     // 支付失败
        const val CODE_PAY_RESULT_ERROR = 116     // 支付结果解析错误
        const val CODE_DATA_EMPTY = 117           // 返回数据为空

        const val MSG_QQ_ID_NULL = "请先配置好QQ_ID"
        const val MSG_WX_ID_NULL = "请先配置好微信ID"
        const val MSG_WB_ID_NULL = "请先配置好微博ID"
    }

}
