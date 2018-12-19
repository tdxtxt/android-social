package com.fungo.socialgo.platform.ali;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.fungo.socialgo.exception.SocialError;
import com.fungo.socialgo.listener.OnPayListener;

import java.util.Map;

/**
 * 支付宝支付
 * Created by tsy on 16/6/1.
 */
public class Alipay {
    private String mParams;
    private PayTask mPayTask;
    private OnPayListener mListener;

    public Alipay(Context context, String params, OnPayListener listener) {
        mParams = params;
        mListener = listener;
        mPayTask = new PayTask((Activity) context);
    }

    //支付
    public void doPay() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> pay_result = mPayTask.payV2(mParams, true);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener == null) {
                            return;
                        }

                        if (pay_result == null) {
                            mListener.onError(new SocialError(SocialError.CODE_PAY_RESULT_ERROR));
                            return;
                        }

                        String resultStatus = pay_result.get("resultStatus");
                        if (TextUtils.equals(resultStatus, "9000")) {    //支付成功
                            mListener.onSuccess();
                        } else if (TextUtils.equals(resultStatus, "8000")) { //支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            mListener.onDealing();
                        } else if (TextUtils.equals(resultStatus, "6001")) {        //支付取消
                            mListener.onCancel();
                        } else if (TextUtils.equals(resultStatus, "6002")) {     //网络连接出错
                            mListener.onError(new SocialError(SocialError.CODE_REQUEST_ERROR));
                        } else if (TextUtils.equals(resultStatus, "4000")) {        //支付错误
                            mListener.onError(new SocialError(SocialError.CODE_PAY_ERROR));
                        }
                    }
                });
            }
        }).start();
    }
}
