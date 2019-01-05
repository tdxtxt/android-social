package com.fungo.socialgo.platform;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * CreateAt : 28/10/2017
 * Describe :
 *
 * @author chendong
 */
public class Target {

    public static final int PLATFORM_QQ = 0x11; // qq
    public static final int PLATFORM_WX = 0x12; // 微信
    public static final int PLATFORM_WB = 0x13; // 微博
    public static final int PLATFORM_ALI = 0x14; // 支付宝

    public static final int LOGIN_QQ = 0x21; // qq 登录
    public static final int LOGIN_WX = 0x22; // 微信登录
    public static final int LOGIN_WB = 0x23; // 微博登录

    public static final int SHARE_QQ_FRIENDS = 0x31; // qq好友
    public static final int SHARE_QQ_ZONE = 0x32; // qq空间
    public static final int SHARE_WX_FRIENDS = 0x33; // 微信好友
    public static final int SHARE_WX_ZONE = 0x34; // 微信朋友圈
    public static final int SHARE_WB = 0x36; // 新浪微博

    public static final int PAY_WX = 0x41;   //微信支付
    public static final int PAY_ALI = 0x42;  // 阿里支付

    public static class Mapping {
        public Mapping(int platform, String creator) {
            this.creator = creator;
            this.platform = platform;
        }

        public String creator;
        public int platform;
    }

    @IntDef({Target.SHARE_QQ_FRIENDS, Target.SHARE_QQ_ZONE,
            Target.SHARE_WX_FRIENDS, Target.SHARE_WX_ZONE, Target.SHARE_WB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareTarget {

    }


    @IntDef({Target.LOGIN_QQ, Target.LOGIN_WB, Target.LOGIN_WX})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoginTarget {

    }

    @IntDef({Target.PAY_WX, Target.PAY_ALI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PayTarget {

    }

    @IntDef({Target.PLATFORM_WX, Target.PLATFORM_QQ, Target.PLATFORM_WB, Target.PLATFORM_ALI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlatformTarget {

    }

    public static int mapPlatform(int target) {
        switch (target) {
            case Target.PLATFORM_QQ:
            case Target.LOGIN_QQ:
            case Target.SHARE_QQ_FRIENDS:
            case Target.SHARE_QQ_ZONE:
                return PLATFORM_QQ;
            case Target.PLATFORM_WX:
            case Target.LOGIN_WX:
            case Target.SHARE_WX_FRIENDS:
            case Target.SHARE_WX_ZONE:
            case Target.PAY_WX:
                return PLATFORM_WX;
            case Target.LOGIN_WB:
            case Target.SHARE_WB:
            case Target.PLATFORM_WB:
                return PLATFORM_WB;
            case Target.PAY_ALI:
            case Target.PLATFORM_ALI:
                return PLATFORM_ALI;
            default:
                return -1;
        }
    }

    public static String toDesc(int target) {
        String result;
        switch (target) {
            case Target.LOGIN_QQ:
                result = "qq登录";
                break;
            case Target.LOGIN_WX:
                result = "微信登录";
                break;
            case Target.LOGIN_WB:
                result = "微博登录";
                break;
            case Target.SHARE_QQ_FRIENDS:
                result = "qq好友分享";
                break;
            case Target.SHARE_QQ_ZONE:
                result = "qq空间分享";
                break;
            case Target.SHARE_WX_FRIENDS:
                result = "微信好友分享";
                break;
            case Target.SHARE_WX_ZONE:
                result = "微信空间分享";
                break;
            case Target.SHARE_WB:
                result = "微博普通分享";
                break;
            default:
                result = "未知类型";
                break;
        }
        return result;
    }
}
