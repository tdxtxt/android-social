package com.fungo.socialgo.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.manager.PayManager.doPay
import com.fungo.socialgo.manager.PlatformManager.KEY_ACTION_TYPE
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.uikit.SocialActivity
import com.fungo.socialgo.utils.SocialLogUtils
import java.lang.ref.WeakReference

/**
 * @author Pinger
 * @since 2018/12/19 16:55
 *
 * 支付管理器
 * 调用[doPay]方法即可发起支付
 */
object PayManager {

    private val TAG = PayManager::class.java.simpleName
    private var mListener: OnPayListener? = null

    fun doPay(context: Context?, payParams: String, @Target.PayTarget payTarget: Int, payListener: OnPayListener?) {
        if (context != null) {
            payListener?.onStart()
            mListener = payListener
            val platform = PlatformManager.makePlatform(context, payTarget)
            if (!platform.isInstall(context)) {
                payListener?.onError(SocialError(SocialError.CODE_NOT_INSTALL))
                return
            }
            val intent = Intent(context, SocialActivity::class.java)
            intent.putExtra(PlatformManager.KEY_ACTION_TYPE, PlatformManager.ACTION_TYPE_PAY)
            intent.putExtra(PlatformManager.KEY_PAY_PARAMS, payParams)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(0, 0)
            }
        }
    }

    /**
     * 激活支付
     */
    fun activePay(activity: Activity) {
        val intent = activity.intent
        val actionType = intent.getIntExtra(KEY_ACTION_TYPE, PlatformManager.INVALID_PARAM)
        val payParams = intent.getStringExtra(PlatformManager.KEY_PAY_PARAMS)
        if (actionType != PlatformManager.ACTION_TYPE_PAY)
            return

        if (mListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnShareListener")
            return
        }

        if (PlatformManager.getPlatform() == null)
            return
        PlatformManager.getPlatform()?.doPay(activity, payParams, FinishPayListener(activity))
    }

    private class FinishPayListener(activity: Activity) : OnPayListener {

        private val mActivityWeakRef: WeakReference<Activity> = WeakReference(activity)

        override fun onStart() {
            mListener?.onStart()
        }

        override fun onSuccess() {
            mListener?.onSuccess()
            finish()
        }

        override fun onError(error: SocialError) {
            mListener?.onError(error)
            finish()
        }

        override fun onCancel() {
            mListener?.onCancel()
            finish()
        }

        private fun finish() {
            PlatformManager.release(mActivityWeakRef.get())
            mListener = null
        }

        override fun onDealing() {
            mListener?.onDealing()
            finish()
        }
    }
}
