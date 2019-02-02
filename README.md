# 第三方登录分享支付SDK
 [ ![Download](https://api.bintray.com/packages/fungo/maven/social-android/images/download.svg) ](https://bintray.com/fungo/maven/social-android/_latestVersion)

在项目中经常会用到一些第三方登录分享的组件，大部分项目同时也会用到支付组件。
登录分享包括最常见的QQ，微信和微博，支付包括微信和支付宝。
这里将这些最基本的组件封装成SDK，供上层使用，底层依赖于第三方的SDK，可以随时替换。

为了能实现更好的解耦，这里对各个第三方平台进行抽离，分成不同的library。
并且提供核心的library供大家自定义其他的平台。


### 优点
* 支持分享登录支付功能。
* 支持单独添加各个平台组件。
* 支持拓展自定义的第三方平台。
* 不需要手动添加activity和更改清单文件。
* 本地保存登录Token，可随时清除。
* 请求用户信息接口可随时迁移到服务器。
* 使用Androidx和Kotlin开发。
* 一行代码实现分享登录支付的调用。


### 添加依赖
#### 添加所有平台
* 全平台(QQ、微信、微博、阿里支付)SDK：

      implementation 'com.pingerx:socialgo:1.0.x'

#### 添加单个平台
* 平台核心库SDK（使用单个平台时必须添加核心库SDK）

      implementation 'com.pingerx:socialgo-core:1.0.x'

* QQ平台SDK

      implementation 'com.pingerx:socialgo-qq:1.0.x'

* 微信平台SDK

      implementation 'com.pingerx:socialgo-wechat:1.0.x'

* 微博平台SDK

      implementation 'com.pingerx:socialgo-weibo:1.0.x'

* 支付宝平台SDK

      implementation 'com.pingerx:socialgo-alipay:1.0.x'


### 使用流程
* 在Application中初始化第三方平台和配置各自的appkey

        val config = SocialGoConfig.create(context)
                .debug(true)
                .qq(QQ_APP_ID)
                .wechat(WX_APP_ID, AppConstant.WX_APP_SECRET)
                .weibo(WEIBO_APP_KEY)

        SocialGo
                .init(config)
                .registerWxPlatform(WxPlatform.Creator())
                .registerWbPlatform(WbPlatform.Creator())
                .registerQQPlatform(QQPlatform.Creator())
                .registerAliPlatform(AliPlatform.Creator())
                .setJsonAdapter(GsonJsonAdapter())
                .setRequestAdapter(OkHttpRequestAdapter())

* 登录

        SocialGo.doLogin(this, Target.LOGIN_QQ) {
            onStart {
                mProgressDialog.show()
                tvConsole?.text = "登录开始"
            }

            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = it.socialUser?.toString()
            }

            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录取消"
            }

            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录异常 + ${it?.errorMsg}"
            }
        }

* 分享

         SocialGo.doShare(this, platformType, shareMedia) {
            onStart { _, _ ->
                mProgressDialog.show()
                tvConsole?.text = "分享开始"
            }
            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享成功"
            }
            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享失败"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (it.errorCode == SocialError.CODE_STORAGE_READ_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
                    } else if (it.errorCode == SocialError.CODE_STORAGE_WRITE_ERROR) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                    }
                }
            }
            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享取消"
            }
        }


* 支付

         SocialGo.doPay(this, params, Target.SHARE_QQ_FRIENDS) {
            onStart {
                tvConsole?.text = "支付开始"
            }
            onSuccess {
                tvConsole?.text = "支付成功"
            }
            onDealing {
                tvConsole?.text = "支付Dealing"
            }
            onFailure {
                tvConsole?.text = "支付异常：${it?.errorMsg}"
            }
            onCancel {
                tvConsole?.text = "支付取消"
            }
        }







### 第三方底层SDK版本
* QQ：`open_sdk_r6019_lite.jar`
* 微信：`com.tencent.mm.opensdk:wechat-sdk-android-without-mta:5.3.1`
* 微博：`com.sina.weibo.sdk:core:4.3.6:openDefaultRelease@aar`
* 支付宝：`com.pingerx:alipay-sdk:1.0.0`
