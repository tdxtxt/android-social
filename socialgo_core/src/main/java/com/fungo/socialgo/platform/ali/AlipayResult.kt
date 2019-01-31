package com.fungo.socialgo.platform.ali

import android.text.TextUtils

/**
 * 阿里支付结果
 */
class AlipayResult(result: String) {

    private var resultStatus: String? = null
    private var result: String? = null
    private var memo: String? = null

    fun getResultStatus(): String? {
        return resultStatus
    }

    fun getResult(): String? {
        return result
    }

    fun getMemo(): String? {
        return memo
    }

    init {
        if (!TextUtils.isEmpty(result)) {
            val resultParams = result.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (resultParam in resultParams) {
                if (resultParam.startsWith("resultStatus")) {
                    resultStatus = gatValue(resultParam, "resultStatus")
                }
                if (resultParam.startsWith("result")) {
                    this.result = gatValue(resultParam, "result")
                }
                if (resultParam.startsWith("memo")) {
                    memo = gatValue(resultParam, "memo")
                }
            }
        }
    }

    override fun toString(): String {
        return ("resultStatus={" + resultStatus + "};memo={" + memo
                + "};result={" + result + "}")
    }

    private fun gatValue(content: String, key: String): String {
        val prefix = "$key={"
        return content.substring(content.indexOf(prefix) + prefix.length,
                content.lastIndexOf("}"))
    }
}
