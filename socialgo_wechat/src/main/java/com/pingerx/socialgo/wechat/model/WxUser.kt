package com.pingerx.socialgo.wechat.model

import com.pingerx.socialgo.core.model.user.BaseSocialUser

/**
 * 微信用户数据
 */
class WxUser : BaseSocialUser() {

    ///////////////////////////////////////////////////////////////////////////
    // openid是同一个公众账号用户的唯一标识，在同一个公众账号中不可能重复，不同公众账号有可能重复
    // unionid是公众账号的标识，是唯一的，即使在不同的公众账号也不可能重复
    ///////////////////////////////////////////////////////////////////////////

    private val unionid: String? = null
    private val openid: String? = null
    private val nickname: String? = null
    private val sex: Int = 0
    private val province: String? = null
    private val city: String? = null
    private val country: String? = null
    private val headimgurl: String? = null
    private val privilege: List<String>? = null
    val errcode: Int = 0
    val errmsg: String? = null

    val isNoError: Boolean
        get() = errcode == 0

    override fun toString(): String {
        return "WxUserInfo{" +
                "openid='" + openid + '\''.toString() +
                ", nickname='" + nickname + '\''.toString() +
                ", sex=" + sex +
                ", province='" + province + '\''.toString() +
                ", city='" + city + '\''.toString() +
                ", country='" + country + '\''.toString() +
                ", headimgurl='" + headimgurl + '\''.toString() +
                ", privilege=" + privilege +
                ", unionid='" + unionid + '\''.toString() +
                '}'.toString()
    }

    override fun getUserId(): String {
        return unionid ?: ""
    }

    override fun getUserNickName(): String {
        return nickname ?: ""
    }

    override fun getUserGender(): Int {
        return when (sex) {
            0 -> BaseSocialUser.GENDER_GIRL
            1 -> BaseSocialUser.GENDER_BOY
            else -> BaseSocialUser.GENDER_UNKONW
        }
    }

    override fun getUserProvince(): String {
        return province ?: ""
    }

    override fun getUserCity(): String {
        return city ?: ""
    }

    override fun getUserHeadUrl(): String {
        return headimgurl ?: ""
    }

    override fun getUserHeadUrlLarge(): String {
        return headimgurl ?: ""
    }
}