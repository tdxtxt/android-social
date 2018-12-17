package com.fungo.sample

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.fungo.imagego.listener.OnImageListener
import com.fungo.imagego.loadBitmap
import com.fungo.socialgo.social.PlatformType
import com.fungo.socialgo.social.SocialApi
import com.fungo.socialgo.social.listener.AuthListener
import com.fungo.socialgo.social.listener.ShareListener
import com.fungo.socialgo.social.share_media.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var platformType: PlatformType = PlatformType.QQ
    private var shareMedia: IShareMedia = ShareTextMedia()


    private val mProgressDialog: ProgressDialog by lazy {
        ProgressDialog(this)
    }

    private var mImageUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1530450487&di=0adadc8f9b25f8f7176a4e79eca56580&src=http://img0.ph.126.net/HTy8QOnZk_jQ9T2wfOEvNA==/3141823690144359477.jpg"
    private var mShareUrl = "https://www.baidu.com/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initEvent()
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
                    shareMedia = ShareTextMedia()

                }
                R.id.rbTypeImage -> {
                    loadBitmap(this, mImageUrl, object : OnImageListener {
                        override fun onFail(msg: String?) {

                        }

                        override fun onSuccess(bitmap: Bitmap?) {
                            shareMedia = ShareImageMedia()
                            (shareMedia as ShareImageMedia).image = bitmap!!
                        }
                    })
                }
                R.id.rbTypeTextImage -> {
                    loadBitmap(this, mImageUrl, object : OnImageListener {
                        override fun onSuccess(bitmap: Bitmap?) {
                            shareMedia = ShareTextImageMedia()
                            (shareMedia as ShareTextImageMedia).image = bitmap!!
                            (shareMedia as ShareTextImageMedia).text = getString(R.string.share_text)
                        }

                        override fun onFail(msg: String?) {
                        }

                    })

                }

                R.id.rbTypeLink -> {
                    loadBitmap(this, mImageUrl, object : OnImageListener {
                        override fun onSuccess(bitmap: Bitmap?) {
                            shareMedia = ShareWebMedia()
                            (shareMedia as ShareWebMedia).thumb = bitmap!!
                            (shareMedia as ShareWebMedia).description = getString(R.string.share_text)
                            (shareMedia as ShareWebMedia).webPageUrl = mShareUrl
                            (shareMedia as ShareWebMedia).title = getString(R.string.share_title)
                        }

                        override fun onFail(msg: String?) {
                        }

                    })

                }
            }
        }

        containerPlatform.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPlatformQQ -> platformType = PlatformType.QQ
                R.id.rbPlatformQzon -> platformType = PlatformType.QZONE
                R.id.rbPlatformWx -> platformType = PlatformType.WEIXIN
                R.id.rbPlatformWxFriend -> platformType = PlatformType.WEIXIN_CIRCLE
                R.id.rbPlatformSina -> platformType = PlatformType.SINA_WB
            }
        }
    }


    fun onQQLogin(view: View) {
        mProgressDialog.show()
        SocialApi.get(this).doOauthVerify(this, PlatformType.QQ, object : AuthListener {
            override fun onComplete(platform_type: PlatformType, map: Map<String, String>) {
                performLoginSuccess(map)
            }

            override fun onError(platform_type: PlatformType, err_msg: String) {
                mProgressDialog.dismiss()
                tvConsole.text = "QQ登录发生错误:$err_msg"
            }

            override fun onCancel(platform_type: PlatformType) {
                mProgressDialog.dismiss()
                tvConsole.text = "QQ登录取消"
            }
        })
    }


    fun onWxLogin(view: View) {
        mProgressDialog.show()
        SocialApi.get(this).doOauthVerify(this, PlatformType.WEIXIN, object : AuthListener {
            override fun onComplete(platform_type: PlatformType, map: Map<String, String>) {
                performLoginSuccess(map)
            }

            override fun onError(platform_type: PlatformType, err_msg: String) {
                mProgressDialog.dismiss()
                tvConsole.text = "WX登录发生错误:$err_msg"
            }

            override fun onCancel(platform_type: PlatformType) {
                mProgressDialog.dismiss()
                tvConsole.text = "WX登录取消"
            }

        })


    }

    fun onSinaLogin(view: View) {
        mProgressDialog.show()
        SocialApi.get(this).doOauthVerify(this, PlatformType.SINA_WB, object : AuthListener {
            override fun onComplete(platform_type: PlatformType, map: Map<String, String>) {
                performLoginSuccess(map)
            }

            override fun onError(platform_type: PlatformType, err_msg: String) {
                mProgressDialog.dismiss()
                tvConsole.text = "WB登录发生错误:$err_msg"
            }

            override fun onCancel(platform_type: PlatformType) {
                mProgressDialog.dismiss()
                tvConsole.text = "WB登录取消"
            }
        })
    }

    fun onShare(view: View) {
        mProgressDialog.show()
        SocialApi.get(this).doShare(this, platformType, shareMedia, object : ShareListener {
            override fun onComplete(platform_type: PlatformType) {
                mProgressDialog.dismiss()
                tvConsole.text = "分享成功"
            }

            override fun onError(platform_type: PlatformType, err_msg: String) {
                mProgressDialog.dismiss()
                tvConsole.text = "分享错误:$err_msg"
            }

            override fun onCancel(platform_type: PlatformType) {
                mProgressDialog.dismiss()
                tvConsole.text = "取消分享"
            }
        })
    }


    fun onPayWx(view: View) {

    }


    fun onPayAli(view: View) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SocialApi.get(this).onActivityResult(requestCode, resultCode, data)
    }


    private fun performLoginSuccess(map: Map<String, String>) {
        mProgressDialog.dismiss()
        if (map.isEmpty()) {
            tvConsole.text = "数据为空"
            return
        }
        val builder = StringBuilder()

        for (key in map.keys) {
            builder.append("$key : ").append("${map[key]}").append("\n")
        }
        tvConsole.text = builder.toString()
    }
}
