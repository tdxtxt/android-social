package com.pingerx.socialgo.weibo.model

import com.pingerx.socialgo.core.model.user.BaseSocialUser

/**
 * 用户信息结构体
 */
class SinaUser : BaseSocialUser() {

    ///////////////////////////////////////////////////////////////////////////
    // 用户UID（int64）
    private val id: String? = null
    // 字符串型的用户 UID
    private val idstr: String? = null
    // 用户昵称
    private val screen_name: String? = null
    // 友好显示名称
    private val name: String? = null
    // 用户所在省级ID
    private val province: Int = 0
    // 用户所在城市ID
    private val city: Int = 0
    // 用户所在地
    private val location: String? = null
    // 用户个人描述
    private val description: String? = null
    // 用户博客地址
    private val url: String? = null
    // 用户头像地址，50×50像素
    private val profile_image_url: String? = null
    // 用户的微博统一URL地址
    private val profile_url: String? = null
    // 用户的个性化域名
    private val domain: String? = null
    //
    private val weihao: String? = null
    // 性别，m：男、f：女、n：未知
    private val gender: String? = null
    // 粉丝数
    private val followers_count: Int = 0
    // 关注数
    private val friends_count: Int = 0
    // 微博数
    private val statuses_count: Int = 0
    // 收藏数
    private val favourites_count: Int = 0
    // 用户创建（注册）时间
    private val created_at: String? = null
    // 暂未支持
    private val following: Boolean = false
    // 是否允许所有人给我发私信，true：是，false：否
    private val allow_all_act_msg: Boolean = false
    // 是否允许标识用户的地理位置，true：是，false：否
    private val geo_enabled: Boolean = false
    // 是否是微博认证用户，即加V用户，true：是，false：否
    private val verified: Boolean = false
    // 暂未支持
    private val verified_type: Int = 0
    // 用户备注信息，只有在查询用户关系时才返回此字段
    private val remark: String? = null
    // 用户的最近一条微博信息字段
    // private Status status;

    // 是否允许所有人对我的微博进行评论，true：是，false：否
    private val allow_all_comment: Boolean = false
    // 用户大头像地址
    private val avatar_large: String? = null
    // 用户高清大头像地址
    private val avatar_hd: String? = null
    // 认证原因
    private val verified_reason: String? = null
    // 该用户是否关注当前登录用户，true：是，false：否
    private val follow_me: Boolean = false
    // 用户的在线状态，0：不在线、1：在线
    private val online_status: Int = 0
    // 用户的互粉数
    private val bi_followers_count: Int = 0
    // 用户当前的语言版本，zh-cn：简体中文，zh-tw：繁体中文，en：英语
    private val lang: String? = null

    /**
     * 注意：以下字段暂时不清楚具体含义，OpenAPI 说明文档暂时没有同步更新对应字段
     */
    private val star: String? = null
    private val mbtype: String? = null
    private val mbrank: String? = null
    private val block_word: String? = null

    override fun getUserId(): String {
        return id ?: ""
    }

    override fun getUserNickName(): String {
        return screen_name ?: ""
    }

    override fun getUserGender(): Int {
        if ("m" == gender)
            return BaseSocialUser.GENDER_BOY
        return if ("f" == gender) BaseSocialUser.GENDER_GIRL else BaseSocialUser.GENDER_UNKONW
    }

    override fun getUserProvince(): String {
        return province.toString() + ""
    }

    override fun getUserCity(): String {
        return city.toString() + ""
    }

    override fun getUserHeadUrl(): String {
        return avatar_large ?: ""
    }

    override fun getUserHeadUrlLarge(): String {
        return avatar_hd ?: ""
    }

    override fun toString(): String {
        return "WbUserInfo{" +
                "id='" + id + '\''.toString() +
                ", idstr='" + idstr + '\''.toString() +
                ", screen_name='" + screen_name + '\''.toString() +
                ", name='" + name + '\''.toString() +
                ", province=" + province +
                ", city=" + city +
                ", location='" + location + '\''.toString() +
                ", description='" + description + '\''.toString() +
                ", url='" + url + '\''.toString() +
                ", profile_image_url='" + profile_image_url + '\''.toString() +
                ", profile_url='" + profile_url + '\''.toString() +
                ", domain='" + domain + '\''.toString() +
                ", weihao='" + weihao + '\''.toString() +
                ", gender='" + gender + '\''.toString() +
                ", followers_count=" + followers_count +
                ", friends_count=" + friends_count +
                ", statuses_count=" + statuses_count +
                ", favourites_count=" + favourites_count +
                ", created_at='" + created_at + '\''.toString() +
                ", following=" + following +
                ", allow_all_act_msg=" + allow_all_act_msg +
                ", geo_enabled=" + geo_enabled +
                ", verified=" + verified +
                ", verified_type=" + verified_type +
                ", remark='" + remark + '\''.toString() +
                ", allow_all_comment=" + allow_all_comment +
                ", avatar_large='" + avatar_large + '\''.toString() +
                ", avatar_hd='" + avatar_hd + '\''.toString() +
                ", verified_reason='" + verified_reason + '\''.toString() +
                ", follow_me=" + follow_me +
                ", online_status=" + online_status +
                ", bi_followers_count=" + bi_followers_count +
                ", lang='" + lang + '\''.toString() +
                ", star='" + star + '\''.toString() +
                ", mbtype='" + mbtype + '\''.toString() +
                ", mbrank='" + mbrank + '\''.toString() +
                ", block_word='" + block_word + '\''.toString() +
                '}'.toString()
    }
}
