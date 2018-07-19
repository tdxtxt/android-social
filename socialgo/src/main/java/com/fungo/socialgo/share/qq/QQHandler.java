package com.fungo.socialgo.share.qq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.fungo.socialgo.share.config.PlatformConfig;
import com.fungo.socialgo.share.config.PlatformType;
import com.fungo.socialgo.share.config.SSOHandler;
import com.fungo.socialgo.share.listener.OnAuthListener;
import com.fungo.socialgo.share.listener.OnShareListener;
import com.fungo.socialgo.share.media.IShareMedia;
import com.fungo.socialgo.share.media.ShareImageMedia;
import com.fungo.socialgo.share.media.ShareMusicMedia;
import com.fungo.socialgo.share.media.ShareWebMedia;
import com.fungo.socialgo.utils.SocialUtils;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * QQ 第三方 Handler
 * Created by tsy on 16/8/18.
 *
 * QQ登录分享官方文档：http://wiki.open.qq.com/index.php?title=Android_API%E8%B0%83%E7%94%A8%E8%AF%B4%E6%98%8E&=45038#1.11_.E5.88.86.E4.BA.AB.E6.B6.88.E6.81.AF.E5.88.B0QQ.EF.BC.88.E6.97.A0.E9.9C.80QQ.E7.99.BB.E5.BD.95.EF.BC.89
 *
 */
public class QQHandler extends SSOHandler {

    private Context mContext;
    private Activity mActivity;

    private Tencent mTencent;

    private PlatformConfig.QQ mConfig;
    private OnAuthListener mAuthListener;
    private OnShareListener mShareListener;

    public QQHandler() {

    }

    @Override
    public void onCreate(Context context, PlatformConfig.Platform config) {
        this.mContext = context;
        this.mConfig = (PlatformConfig.QQ) config;

        this.mTencent = Tencent.createInstance(mConfig.appId, mContext);
    }

    @Override
    public void authorize(Activity activity, OnAuthListener authListener) {
        this.mActivity = activity;
        this.mAuthListener = authListener;

        this.mTencent.login(this.mActivity, "all", new IUiListener() {
            @Override
            public void onComplete(Object o) {
                if (null == o) {
                    SocialUtils.INSTANCE.e("onComplete response=null");
                    mAuthListener.onError(mConfig.getName(), "onComplete response=null");
                    return;
                }

                JSONObject response = (JSONObject) o;

                initOpenidAndToken(response);

                mAuthListener.onComplete(mConfig.getName(), SocialUtils.INSTANCE.jsonToMap(response));

                mTencent.logout(mActivity);
            }

            @Override
            public void onError(UiError uiError) {
                String errmsg = "errcode=" + uiError.errorCode + " errmsg=" + uiError.errorMessage + " errdetail=" + uiError.errorDetail;
                SocialUtils.INSTANCE.e(errmsg);
                mAuthListener.onError(mConfig.getName(), errmsg);
            }

            @Override
            public void onCancel() {
                mAuthListener.onCancel(mConfig.getName());
            }
        });
    }

    @Override
    public void share(Activity activity, IShareMedia shareMedia, OnShareListener shareListener) {
        this.mActivity = activity;
        this.mShareListener = shareListener;

        //获取当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String strDate = formatter.format(curDate);

        final String path = Environment.getExternalStorageDirectory().toString() + "/socail_qq_img_tmp" + strDate + ".png";
        final File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        Bundle params = new Bundle();

        if (this.mConfig.getName() == PlatformType.QZONE) {      //qq空间
            if (shareMedia instanceof ShareWebMedia) {          // 网页分享
                ShareWebMedia shareWebMedia = (ShareWebMedia) shareMedia;

                // 图片保存本地
                SocialUtils.INSTANCE.saveBitmapFile(shareWebMedia.getThumb(), path);

                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareWebMedia.getTitle());
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareWebMedia.getDescription());
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareWebMedia.getWebPageUrl());

                ArrayList<String> path_arr = new ArrayList<>();
                path_arr.add(path);
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, path_arr);  //!这里是大坑 不能用SHARE_TO_QQ_IMAGE_LOCAL_URL
            } else {
                if (this.mShareListener != null) {
                    this.mShareListener.onError(this.mConfig.getName(), "QZone is not support this shareMedia");
                }
                return;
            }

            //qq zone分享
            this.mTencent.shareToQzone(this.mActivity, params, new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    mShareListener.onComplete(mConfig.getName());

                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    String errmsg = "errcode=" + uiError.errorCode + " errmsg=" + uiError.errorMessage + " errdetail=" + uiError.errorDetail;
                    SocialUtils.INSTANCE.e(errmsg);
                    mShareListener.onError(mConfig.getName(), errmsg);

                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onCancel() {
                    mShareListener.onCancel(mConfig.getName());

                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        } else {        //分享到qq
            if (shareMedia instanceof ShareWebMedia) {       //网页分享
                ShareWebMedia shareWebMedia = (ShareWebMedia) shareMedia;

                //图片保存本地
                SocialUtils.INSTANCE.saveBitmapFile(shareWebMedia.getThumb(), path);

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, shareWebMedia.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareWebMedia.getDescription());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareWebMedia.getWebPageUrl());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
            } else if (shareMedia instanceof ShareImageMedia) {  //图片分享
                ShareImageMedia shareImageMedia = (ShareImageMedia) shareMedia;

                //图片保存本地
                SocialUtils.INSTANCE.saveBitmapFile(shareImageMedia.getImage(), path);

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
            } else if (shareMedia instanceof ShareMusicMedia) {  //音乐分享
                ShareMusicMedia shareMusicMedia = (ShareMusicMedia) shareMedia;

                //图片保存本地
                SocialUtils.INSTANCE.saveBitmapFile(shareMusicMedia.getThumb(), path);

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, shareMusicMedia.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareMusicMedia.getDescription());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareMusicMedia.getMusicUrl());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
                params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, shareMusicMedia.getMusicUrl());
            } else {
                if (this.mShareListener != null) {
                    this.mShareListener.onError(this.mConfig.getName(), "QQ is not support this shareMedia");
                }
                return;
            }

            //qq分享
            mTencent.shareToQQ(mActivity, params, new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    mShareListener.onComplete(mConfig.getName());

                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    String errmsg = "errcode=" + uiError.errorCode + " errmsg=" + uiError.errorMessage + " errdetail=" + uiError.errorDetail;
                    SocialUtils.INSTANCE.e(errmsg);
                    mShareListener.onError(mConfig.getName(), errmsg);

                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onCancel() {
                    mShareListener.onCancel(mConfig.getName());

                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, null);
    }

    //要初始化open_id和token
    private void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);

            mTencent.setAccessToken(token, expires);
            mTencent.setOpenId(openId);
        } catch (Exception e) {
        }
    }
}
