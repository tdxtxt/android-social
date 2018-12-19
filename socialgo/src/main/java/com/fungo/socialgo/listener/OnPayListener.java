package com.fungo.socialgo.listener;

import com.fungo.socialgo.exception.SocialError;

/**
 * @author Pinger
 * @since 2018/12/19 16:16
 */
public interface OnPayListener {

    void onStart();

    void onSuccess(); //支付成功

    void onDealing();    //正在处理中 小概率事件 此时以验证服务端异步通知结果为准

    void onError(SocialError error);   //支付失败

    void onCancel();    //支付取消

}
