package com.fungo.socialgo.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.manager.LoginManager.login
import com.fungo.socialgo.model.LoginResult
import com.fungo.socialgo.model.token.AccessToken
import com.fungo.socialgo.platform.Target
import com.fungo.socialgo.uikit.SocialActivity
import com.fungo.socialgo.utils.SocialLogUtils
import java.lang.ref.WeakReference


/**
 * 登录操作管理类
 * 直接调用[login]方法即可
 */
object LoginManager {

    private val TAG: String = LoginManager::class.java.simpleName
    private var mListener: OnLoginListener? = null

    /**
     * 开始登陆
     *
     * @param context       上下文
     * @param loginTarget   登录类型
     * @param loginListener 登录回调
     */
    fun login(context: Context?, @Target.LoginTarget loginTarget: Int, loginListener: OnLoginListener?) {
        if (context != null) {
            loginListener?.onStart()
            mListener = loginListener
            val platform = PlatformManager.makePlatform(context, loginTarget)
            if (!platform.isInstall(context)) {
                loginListener?.onFailure(SocialError(SocialError.CODE_NOT_INSTALL))
                return
            }
            val intent = Intent(context, SocialActivity::class.java)
            intent.putExtra(PlatformManager.KEY_ACTION_TYPE, PlatformManager.ACTION_TYPE_LOGIN)
            intent.putExtra(PlatformManager.KEY_LOGIN_TARGET, loginTarget)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(0, 0)
            }
        } else {
            SocialLogUtils.e(TAG, "context not be null...")
        }
    }

    /**
     * 激活登陆
     */
    fun activeLogin(activity: Activity) {
        val intent = activity.intent
        val actionType = intent?.getIntExtra(PlatformManager.KEY_ACTION_TYPE, PlatformManager.INVALID_PARAM)
        val loginTarget = intent?.getIntExtra(PlatformManager.KEY_LOGIN_TARGET, PlatformManager.INVALID_PARAM)
        if (actionType == PlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "activeLogin actionType无效")
            return
        }
        if (actionType != PlatformManager.ACTION_TYPE_LOGIN) {
            return
        }
        if (loginTarget == PlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "shareTargetType无效")
            return
        }
        if (mListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnLoginListener")
            return
        }
        if (PlatformManager.getPlatform() == null) {
            return
        }
        PlatformManager.getPlatform()?.login(activity, FinishLoginListener(activity))
    }

    private class FinishLoginListener constructor(activity: Activity) : OnLoginListener {

        private val mActivityWeakRef: WeakReference<Activity> = WeakReference(activity)

        override fun onStart() {
            mListener?.onStart()
        }

        private fun finish() {
            PlatformManager.release(mActivityWeakRef.get())
            mListener = null
        }

        override fun onSuccess(loginResult: LoginResult) {
            mListener?.onSuccess(loginResult)
            finish()
        }

        override fun onCancel() {
            mListener?.onCancel()
            finish()
        }

        override fun onFailure(e: SocialError) {
            mListener?.onFailure(e)
            finish()
        }
    }

    fun clearAllToken(context: Context?) {
        AccessToken.clearToken(context, Target.LOGIN_QQ)
        AccessToken.clearToken(context, Target.LOGIN_WX)
        AccessToken.clearToken(context, Target.LOGIN_WB)
    }

    fun clearToken(context: Context?, @Target.LoginTarget loginTarget: Int) {
        AccessToken.clearToken(context, loginTarget)
    }

}
