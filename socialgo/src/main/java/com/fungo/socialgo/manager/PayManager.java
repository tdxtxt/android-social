package com.fungo.socialgo.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.fungo.socialgo.exception.SocialError;
import com.fungo.socialgo.listener.OnPayListener;
import com.fungo.socialgo.platform.IPlatform;
import com.fungo.socialgo.platform.Target;
import com.fungo.socialgo.uikit.ActionActivity;
import com.fungo.socialgo.utils.SocialLogUtils;

import java.lang.ref.WeakReference;

import static com.fungo.socialgo.manager.PlatformManager.KEY_ACTION_TYPE;

/**
 * @author Pinger
 * @since 2018/12/19 16:55
 */
public class PayManager {

    public static final String TAG = PayManager.class.getSimpleName();

    static OnPayListener mListener;

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void doPay(Context context, String payParams, @Target.PayTarget int payTarget, OnPayListener payListener) {
        payListener.onStart();
        mListener = payListener;
        IPlatform platform = PlatformManager.makePlatform(context, payTarget);
        if (!platform.isInstall(context)) {
            payListener.onError(new SocialError(SocialError.CODE_NOT_INSTALL));
            return;
        }
        Intent intent = new Intent(context, ActionActivity.class);
        intent.putExtra(PlatformManager.KEY_ACTION_TYPE, PlatformManager.ACTION_TYPE_PAY);
        intent.putExtra(PlatformManager.KEY_PAY_PARAMS, payParams);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    /**
     * 激活支付
     */
    static void _actionPay(Activity activity) {
        Intent intent = activity.getIntent();
        int actionType = intent.getIntExtra(KEY_ACTION_TYPE, PlatformManager.INVALID_PARAM);
        String payParams = intent.getStringExtra(PlatformManager.KEY_PAY_PARAMS);
        if (actionType != PlatformManager.ACTION_TYPE_PAY)
            return;

        if (mListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnShareListener");
            return;
        }

        if (PlatformManager.getPlatform() == null)
            return;
        PlatformManager.getPlatform().doPay(activity, payParams, new FinishPayListener((activity)));
    }

    static class FinishPayListener implements OnPayListener {

        private WeakReference<Activity> mActivityWeakRef;

        FinishPayListener(Activity activity) {
            mActivityWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void onStart() {
            if (mListener != null) mListener.onStart();
        }

        @Override
        public void onSuccess() {
            if (mListener != null) mListener.onSuccess();
            finish();
        }

        @Override
        public void onError(SocialError error) {
            if (mListener != null) mListener.onError(error);
            finish();
        }

        @Override
        public void onCancel() {
            if (mListener != null) mListener.onCancel();
            finish();
        }

        private void finish() {
            PlatformManager.release(mActivityWeakRef.get());
            mListener = null;
        }

        @Override
        public void onDealing() {
            if (mListener != null) mListener.onDealing();
            finish();
        }
    }
}
