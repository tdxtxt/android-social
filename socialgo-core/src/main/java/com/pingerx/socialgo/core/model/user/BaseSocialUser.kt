package com.pingerx.socialgo.core.model.user

/**
 * 用户实体的基类
 */
abstract class BaseSocialUser {

    abstract fun getUserId(): String

    abstract fun getUserNickName(): String

    abstract fun getUserGender(): Int

    abstract fun getUserProvince(): String

    abstract fun getUserCity(): String

    abstract fun getUserHeadUrl(): String

    abstract fun getUserHeadUrlLarge(): String


    override fun toString(): String {
        return "BaseSocialUser{" +
                "userId='" + getUserId() + '\''.toString() +
                ", userNickName='" + getUserNickName() + '\''.toString() +
                ", userGender=" + getUserGender() +
                ", userProvince='" + getUserProvince() + '\''.toString() +
                ", userCity='" + getUserCity() + '\''.toString() +
                ", userHeadUrl='" + getUserHeadUrl() + '\''.toString() +
                ", userHeadUrlLarge='" + getUserHeadUrlLarge() + '\''.toString() +
                '}'.toString()
    }

    companion object {
        const val GENDER_BOY = 1
        const val GENDER_GIRL = 2
        const val GENDER_UNKONW = 0
    }
}
