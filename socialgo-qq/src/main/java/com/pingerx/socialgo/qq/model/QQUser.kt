package com.pingerx.socialgo.qq.model

import com.pingerx.socialgo.core.model.user.BaseSocialUser


/**
 * QQ登录用户的用户信息
 */
class QQUser : BaseSocialUser() {

    private var openId: String? = null
    private val ret: Int = 0
    private val msg: String? = null
    private val is_lost: Int = 0
    private val nickname: String? = null
    private val gender: String? = null
    private val province: String? = null
    private val city: String? = null
    private val figureurl: String? = null
    private val figureurl_1: String? = null
    private val figureurl_2: String? = null
    private val figureurl_qq_1: String? = null// 50.的用户头像
    private val figureurl_qq_2: String? = null// 100.用户头像
    private val is_yellow_vip: String? = null
    private val vip: String? = null
    private val yellow_vip_level: String? = null
    private val level: String? = null
    private val is_yellow_year_vip: String? = null

    override fun getUserId(): String {
        return openId ?: ""
    }

    fun setOpenId(openId: String) {
        this.openId = openId
    }

    override fun getUserNickName(): String {
        return nickname ?: ""
    }

    override fun getUserGender(): Int {
        if ("女" == gender) {
            return BaseSocialUser.GENDER_GIRL
        }
        return if ("男" == gender) {
            BaseSocialUser.GENDER_BOY
        } else BaseSocialUser.GENDER_UNKONW
    }

    override fun getUserProvince(): String {
        return province ?: ""
    }

    override fun getUserCity(): String {
        return city ?: ""
    }

    override fun getUserHeadUrl(): String {
        return figureurl_qq_1 ?: ""
    }

    override fun getUserHeadUrlLarge(): String {
        return figureurl_qq_2 ?: ""
    }

    override fun toString(): String {
        return "QQUserInfo{" +
                "ret=" + ret +
                ", msg='" + msg + '\''.toString() +
                ", is_lost=" + is_lost +
                ", nickname='" + nickname + '\''.toString() +
                ", gender='" + gender + '\''.toString() +
                ", province='" + province + '\''.toString() +
                ", city='" + city + '\''.toString() +
                ", figureurl='" + figureurl + '\''.toString() +
                ", figureurl_1='" + figureurl_1 + '\''.toString() +
                ", figureurl_2='" + figureurl_2 + '\''.toString() +
                ", figureurl_qq_1='" + figureurl_qq_1 + '\''.toString() +
                ", figureurl_qq_2='" + figureurl_qq_2 + '\''.toString() +
                ", is_yellow_vip='" + is_yellow_vip + '\''.toString() +
                ", vip='" + vip + '\''.toString() +
                ", yellow_vip_level='" + yellow_vip_level + '\''.toString() +
                ", level='" + level + '\''.toString() +
                ", is_yellow_year_vip='" + is_yellow_year_vip + '\''.toString() +
                '}'.toString()
    }
}
