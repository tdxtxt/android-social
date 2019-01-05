package com.fungo.sample

import android.Manifest
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fungo.socialgo.exception.SocialError
import com.fungo.socialgo.listener.OnLoginListener
import com.fungo.socialgo.listener.OnPayListener
import com.fungo.socialgo.listener.OnShareListener
import com.fungo.socialgo.manager.LoginManager
import com.fungo.socialgo.manager.PayManager
import com.fungo.socialgo.manager.ShareManager
import com.fungo.socialgo.model.LoginResult
import com.fungo.socialgo.model.ShareObj
import com.fungo.socialgo.platform.Target
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var platformType: Int = Target.SHARE_QQ_FRIENDS
    private lateinit var shareMedia: ShareObj


    private val mProgressDialog: ProgressDialog by lazy {
        ProgressDialog(this)
    }

    private var mImageUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1530450487&di=0adadc8f9b25f8f7176a4e79eca56580&src=http://img0.ph.126.net/HTy8QOnZk_jQ9T2wfOEvNA==/3141823690144359477.jpg"
    private var mShareUrl = "https://www.baidu.com/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initEvent()
        initView()
    }


    private fun initView() {
        tvConsole.movementMethod = ScrollingMovementMethod.getInstance()
        rbTypeText.isChecked = true
        rbPlatformQQ.isChecked = true
    }

    private fun initEvent() {
        containerType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTypeText -> {
                    shareMedia = ShareObj.buildTextObj("我是文字分享的标题", "我是文字分享的内容")
                }
                R.id.rbTypeImage -> {
                    shareMedia = ShareObj.buildImageObj(mImageUrl, "我是分享图片的描述")
                }
                R.id.rbTypeTextImage -> {
                    shareMedia = ShareObj.buildImageObj(mImageUrl, getString(R.string.share_text))
                }

                R.id.rbTypeLink -> {
                    shareMedia = ShareObj.buildWebObj(getString(R.string.share_title), getString(R.string.share_text), mImageUrl, mShareUrl)
                }
            }
        }

        containerPlatform.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPlatformQQ -> platformType = Target.SHARE_QQ_FRIENDS
                R.id.rbPlatformQzon -> platformType = Target.SHARE_QQ_ZONE
                R.id.rbPlatformWx -> platformType = Target.SHARE_WX_ZONE
                R.id.rbPlatformWxFriend -> platformType = Target.SHARE_WX_FRIENDS
                R.id.rbPlatformSina -> platformType = Target.SHARE_WB
            }
        }
    }

    fun onQQLogin(view: View) {
        LoginManager.login(this, Target.LOGIN_QQ, LoginListener())
    }

    fun onWxLogin(view: View) {
        LoginManager.login(this, Target.LOGIN_WX, LoginListener())
    }

    fun onSinaLogin(view: View) {
        LoginManager.login(this, Target.LOGIN_WB, LoginListener())
    }

    inner class LoginListener : OnLoginListener {
        override fun onStart() {
            mProgressDialog.show()
            tvConsole?.text = "登录开始"
        }

        override fun onSuccess(loginResult: LoginResult?) {
            mProgressDialog.dismiss()
            tvConsole?.text = loginResult?.socialUser?.toString()
        }

        override fun onCancel() {
            mProgressDialog.dismiss()
            tvConsole?.text = "登录取消"
        }

        override fun onFailure(e: SocialError?) {
            mProgressDialog.dismiss()
            tvConsole?.text = "登录异常 + ${e?.errorMsg}"
        }
    }


    fun onShare(view: View) {
        mProgressDialog.show()
        ShareManager.share(this, platformType, shareMedia, object : OnShareListener {
            override fun onStart(shareTarget: Int, obj: ShareObj?) {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享开始"
            }

            override fun onPrepareInBackground(shareTarget: Int, obj: ShareObj?): ShareObj {
                return obj!!
            }

            override fun onSuccess() {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享成功"
            }

            override fun onFailure(e: SocialError?) {
                tvConsole?.text = "分享失败"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (e?.errorCode == SocialError.CODE_STORAGE_READ_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
                    } else if (e?.errorCode == SocialError.CODE_STORAGE_WRITE_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                    }
                }
            }

            override fun onCancel() {
                tvConsole?.text = "分享取消"
            }
        })
    }


    fun onPayWx(view: View) {
        PayManager.doPay(this, "xxxxx", Target.PAY_WX, PayListener())
    }


    fun onPayAli(view: View) {
        PayManager.doPay(this, "xxxxx", Target.PAY_ALI, PayListener())
    }


    inner class PayListener : OnPayListener {

        override fun onStart() {
            tvConsole?.text = "支付开始"
        }

        override fun onSuccess() {
            tvConsole?.text = "支付成功"
        }

        override fun onDealing() {
            tvConsole?.text = "onDealing"
        }

        override fun onError(error: SocialError?) {
            tvConsole?.text = "支付异常：${error?.errorMsg}"
        }

        override fun onCancel() {
            tvConsole?.text = "支付取消"
        }
    }


}
