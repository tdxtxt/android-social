package com.pingerx.sample

import android.Manifest
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.pingerx.socialgo.core.SocialGo
import com.pingerx.socialgo.core.exception.SocialError
import com.pingerx.socialgo.core.model.ShareEntity
import com.pingerx.socialgo.core.platform.Target
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var platformType: Int = Target.SHARE_QQ_FRIENDS
    private lateinit var shareMedia: ShareEntity


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
                    shareMedia = ShareEntity.buildTextObj("我是文字分享的标题", "我是文字分享的内容")
                }
                R.id.rbTypeImage -> {
                    shareMedia = ShareEntity.buildImageObj(mImageUrl, "我是分享图片的描述")
                }
                R.id.rbTypeTextImage -> {
                    shareMedia = ShareEntity.buildImageObj(mImageUrl, getString(R.string.share_text))
                }

                R.id.rbTypeLink -> {
                    shareMedia = ShareEntity.buildWebObj(getString(R.string.share_title), getString(R.string.share_text), mImageUrl, mShareUrl)
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
        doLogin(Target.LOGIN_QQ)
    }

    fun onWxLogin(view: View) {
        doLogin(Target.LOGIN_WX)
    }

    fun onSinaLogin(view: View) {
        doLogin(Target.LOGIN_WB)
    }

    private fun doLogin(@Target.LoginTarget loginTarget: Int) {
        SocialGo.doLogin(this, loginTarget) {
            onStart {
                mProgressDialog.show()
                tvConsole?.text = "登录开始"
            }

            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = it.socialUser?.toString()
            }

            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录取消"
            }

            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录异常 + ${it.errorMsg}"
            }
        }
    }

    fun onShare(view: View) {
        SocialGo.doShare(this, platformType, shareMedia) {
            onStart { _, _ ->
                mProgressDialog.show()
                tvConsole?.text = "分享开始"
            }
            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享成功"
            }
            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享失败"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (it.errorCode == SocialError.CODE_STORAGE_READ_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
                    } else if (it.errorCode == SocialError.CODE_STORAGE_WRITE_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                    }
                }
            }
            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享取消"
            }
        }
    }

    fun onPayWx(view: View) {
        doPay(Target.PAY_WX)
    }

    fun onPayAli(view: View) {
        doPay(Target.PAY_ALI)
    }

    private fun doPay(@Target.PayTarget payTarget: Int) {
        SocialGo.doPay(this, "xxxxx", payTarget) {

            onStart {
                tvConsole?.text = "支付开始"
            }

            onSuccess {
                tvConsole?.text = "支付成功"
            }

            onDealing {
                tvConsole?.text = "onDealing"
            }

            onFailure {
                tvConsole?.text = "支付异常：${it.errorMsg}"
            }

            onCancel {
                tvConsole?.text = "支付取消"
            }

        }
    }
}
