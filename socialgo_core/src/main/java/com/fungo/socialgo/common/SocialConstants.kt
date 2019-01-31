package com.fungo.socialgo.common

/**
 * 常用常量
 * 包括第三方页面的包名和权限范围等
 */
object SocialConstants {

    const val QQ_CREATOR = "com.fungo.socialgo.platform.qq.QQPlatform\$Creator"
    const val WX_CREATOR = "com.fungo.socialgo.platform.wechat.WxPlatform\$Creator"
    const val WB_CREATOR = "com.fungo.socialgo.platform.weibo.WbPlatform\$Creator"
    const val ALI_CREATOR = "com.fungo.socialgo.platform.ali.AliPlatform\$Creator"

    const val QQ_PKG = "com.tencent.mobileqq"
    const val WECHAT_PKG = "com.tencent.mm"
    const val SINA_PKG = "com.sina.weibo"

    // 微信选择好友
    const val WX_FRIEND_PAGE = "com.tencent.mm.ui.tools.ShareImgUI"
    // 微信主界面
    const val WX_LAUNCH_PAGE = "com.tencent.mm.ui.LauncherUI"

    const val QQ_QZONE_PAGE = "com.qzonex.module.maxvideo.activity.QzonePublishVideoActivity"// qq空间app
    const val QQ_BROWSER_FAST_TRANS_PAGE = "com.tencent.mtt.browser.share.inhost.FastSpreadEntryActivity"//qq浏览器跨屏穿越
    const val QQ_FRIENDS_PAGE = "com.tencent.mobileqq.activity.JumpActivity"//qq选择好友、群、我的电脑
    const val QQ_COMPUTER_FILE_PAGE = "com.tencent.mobileqq.activity.qfileJumpActivity"// 发送到我的电脑
    const val QQ_TRANSLATE_FACE_2_FACE_PAGE = "cooperation.qlink.QlinkShareJumpActivity"//qq面对面快传
    const val QQ_FAVORITE_PAGE = "cooperation.qqfav.widget.QfavJumpActivity"// 保存到qq收藏

    // 发送微博界面
    const val WB_COMPOSER_PAGE = "com.sina.weibo.composerinde.ComposerDispatchActivity"
    // 微博故事
    const val WB_STORY_PAGE = "com.sina.weibo.story.publisher.StoryDispatcher"

    /**
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     */
    const val REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    const val SCOPE = "all"
}
