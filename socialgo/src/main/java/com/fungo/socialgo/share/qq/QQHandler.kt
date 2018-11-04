package com.fungo.socialgo.share.qq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import com.fungo.socialgo.share.config.PlatformConfig
import com.fungo.socialgo.share.config.PlatformType
import com.fungo.socialgo.share.config.SSOHandler
import com.fungo.socialgo.share.listener.OnAuthListener
import com.fungo.socialgo.share.listener.OnShareListener
import com.fungo.socialgo.share.media.IShareMedia
import com.fungo.socialgo.share.media.ShareImageMedia
import com.fungo.socialgo.share.media.ShareMusicMedia
import com.fungo.socialgo.share.media.ShareWebMedia
import com.fungo.socialgo.utils.SocialUtils
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Pinger
 * @since 18-7-20 下午3:31
 * 登录登录分享处理
 *
 * QQ登录分享官方文档：http://wiki.open.qq.com/index.php?title=Android_API%E8%B0%83%E7%94%A8%E8%AF%B4%E6%98%8E&=45038#1.11_.E5.88.86.E4.BA.AB.E6.B6.88.E6.81.AF.E5.88.B0QQ.EF.BC.88.E6.97.A0.E9.9C.80QQ.E7.99.BB.E5.BD.95.EF.BC.89
 */

class QQHandler : SSOHandler() {

    private var mContext: Context? = null
    private var mActivity: Activity? = null

    private var mTencent: Tencent? = null

    private var mConfig: PlatformConfig.QQ? = null
    private var mAuthListener: OnAuthListener? = null
    private var mShareListener: OnShareListener? = null


    override fun onCreate(context: Context, config: PlatformConfig.Platform) {
        this.mContext = context
        this.mConfig = config as PlatformConfig.QQ

        this.mTencent = Tencent.createInstance(mConfig!!.appId, mContext)
    }


    override fun authorize(activity: Activity, authListener: OnAuthListener) {
        this.mActivity = activity
        this.mAuthListener = authListener

        val mediaType = mConfig?.name ?: PlatformType.QQ
        this.mTencent?.login(activity, "all", object : IUiListener {
            override fun onComplete(any: Any?) {
                if (null == any) {
                    SocialUtils.e("onComplete response=null")
                    mAuthListener?.onError(mediaType, "onComplete response=null")
                    return
                }

                val response = any as JSONObject

                initOpenidAndToken(response)

                mAuthListener?.onComplete(mediaType, SocialUtils.jsonToMap(response))

                mTencent?.logout(mActivity)
            }

            override fun onCancel() {
                mAuthListener?.onCancel(mediaType)
            }

            override fun onError(uiError: UiError?) {
                val errMsg = "errcode=" + uiError?.errorCode + " errmsg=" + uiError?.errorMessage + " errdetail=" + uiError?.errorDetail
                SocialUtils.e(errMsg)
                mAuthListener?.onError(mediaType, errMsg)
            }
        })

    }


    override fun share(activity: Activity, shareMedia: IShareMedia, shareListener: OnShareListener) {
        this.mActivity = activity
        this.mShareListener = shareListener

        //获取当前时间
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
        val curDate = Date(System.currentTimeMillis())
        val strDate = formatter.format(curDate)

        val path = Environment.getExternalStorageDirectory().toString() + "/socail_qq_img_tmp" + strDate + ".png"
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }

        val params = Bundle()
        val mediaType: PlatformType
        if (this.mConfig?.name == PlatformType.QZONE) {      //qq空间
            mediaType = PlatformType.QZONE
            if (shareMedia is ShareWebMedia) {          // 网页分享
                val (url, title, description, thumb) = shareMedia

                // 图片保存本地
                SocialUtils.saveBitmapFile(thumb, path)

                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title)
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description)
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url)

                val path_arr = ArrayList<String>()
                path_arr.add(path)
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, path_arr)  //!这里是大坑 不能用SHARE_TO_QQ_IMAGE_LOCAL_URL
            } else {
                this.mShareListener?.onError(mediaType, "QZone is not support this shareMedia")
                return
            }

            //qq zone分享
            this.mTencent?.shareToQzone(this.mActivity, params, object : IUiListener {
                override fun onComplete(o: Any) {
                    mShareListener?.onComplete(mediaType)

                    if (file.exists()) {
                        file.delete()
                    }
                }

                override fun onError(uiError: UiError) {
                    val errmsg = "errcode=" + uiError.errorCode + " errmsg=" + uiError.errorMessage + " errdetail=" + uiError.errorDetail
                    SocialUtils.e(errmsg)
                    mShareListener?.onError(mediaType, errmsg)

                    if (file.exists()) {
                        file.delete()
                    }
                }

                override fun onCancel() {
                    mShareListener?.onCancel(mediaType)

                    if (file.exists()) {
                        file.delete()
                    }
                }
            })
        } else {
            mediaType = PlatformType.QQ
            //分享到qq
            if (shareMedia is ShareWebMedia) {       //网页分享
                val (url, title, description, thumb) = shareMedia

                //图片保存本地
                SocialUtils.saveBitmapFile(thumb, path)

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description)
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url)
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path)
            } else if (shareMedia is ShareImageMedia) {  //图片分享
                val (bitmap) = shareMedia

                // 图片保存本地
                SocialUtils.saveBitmapFile(bitmap, path)

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path)
            } else if (shareMedia is ShareMusicMedia) {  //音乐分享
                val (musicUrl, title, description, thumb) = shareMedia

                //图片保存本地
                SocialUtils.saveBitmapFile(thumb, path)

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO)
                params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description)
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, musicUrl)
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path)
                params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, musicUrl)
            } else {
                this.mShareListener?.onError(mediaType, "QQ is not support this shareMedia")
                return
            }

            //qq分享
            mTencent?.shareToQQ(mActivity, params, object : IUiListener {
                override fun onComplete(o: Any) {
                    mShareListener?.onComplete(mediaType)

                    if (file.exists()) {
                        file.delete()
                    }
                }

                override fun onError(uiError: UiError) {
                    val errmsg = "errcode=" + uiError.errorCode + " errmsg=" + uiError.errorMessage + " errdetail=" + uiError.errorDetail
                    SocialUtils.e(errmsg)
                    mShareListener?.onError(mediaType, errmsg)

                    if (file.exists()) {
                        file.delete()
                    }
                }

                override fun onCancel() {
                    mShareListener?.onCancel(mediaType)

                    if (file.exists()) {
                        file.delete()
                    }
                }
            })
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, null)
    }


    /**
     * 初始化open_id和token
     */
    private fun initOpenidAndToken(jsonObject: JSONObject) {
        try {
            val token = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN)
            val expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN)
            val openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID)

            mTencent?.setAccessToken(token, expires)
            mTencent?.openId = openId
        } catch (e: Exception) {
            SocialUtils.e(e.message)
        }

    }
}