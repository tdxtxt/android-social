package com.fungo.socialgo

import android.content.Context
import android.util.SparseArray
import com.fungo.socialgo.adapter.IJsonAdapter
import com.fungo.socialgo.adapter.IRequestAdapter
import com.fungo.socialgo.adapter.impl.DefaultRequestAdapter
import com.fungo.socialgo.common.SocialConstants
import com.fungo.socialgo.platform.IPlatform
import com.fungo.socialgo.platform.PlatformCreator
import com.fungo.socialgo.platform.Target
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * 登录分享组件入口
 *
 * 初始化和设置配置，注册平台
 */

object SocialSdk {

    private var sSocialSdkConfig: SocialSdkConfig? = null
    private var jsonAdapter: IJsonAdapter? = null
    private var requestAdapter: IRequestAdapter? = null

    private var sPlatformCreatorMap: SparseArray<PlatformCreator>? = null
    private var sExecutorService: ExecutorService? = null

    fun getExecutor(): ExecutorService {
        if (sExecutorService == null) {
            sExecutorService = Executors.newCachedThreadPool()
        }
        return sExecutorService!!
    }

    fun getConfig(): SocialSdkConfig {
        if (sSocialSdkConfig == null) {
            throw IllegalStateException("invoke SocialSdk.init() first please")
        }
        return sSocialSdkConfig!!
    }


    fun init(config: SocialSdkConfig) {
        sSocialSdkConfig = config
        actionRegisterPlatform()
    }


    ///////////////////////////////////////////////////////////////////////////
    // Platform 注册
    ///////////////////////////////////////////////////////////////////////////
    private fun actionRegisterPlatform() {
        if (sPlatformCreatorMap == null) {
            sPlatformCreatorMap = SparseArray()
        }
        val mappings = arrayOf(Target.Mapping(Target.PLATFORM_QQ, SocialConstants.QQ_CREATOR), Target.Mapping(Target.PLATFORM_WX, SocialConstants.WX_CREATOR), Target.Mapping(Target.PLATFORM_WB, SocialConstants.WB_CREATOR), Target.Mapping(Target.PLATFORM_ALI, SocialConstants.ALI_CREATOR))
        val disablePlatforms = sSocialSdkConfig!!.getDisablePlatforms()
        for (mapping in mappings) {
            if (!disablePlatforms!!.contains(mapping.platform)) {
                val creator = makeCreator(mapping.creator)
                if (creator != null) {
                    sPlatformCreatorMap!!.put(mapping.platform, creator)
                }

            }
        }
    }

    private fun makeCreator(clazz: String): PlatformCreator? {
        try {
            return Class.forName(clazz).newInstance() as PlatformCreator
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun getPlatform(context: Context, target: Int): IPlatform? {
        val creator = sPlatformCreatorMap?.get(target)
        return creator?.create(context, target)
    }


    ///////////////////////////////////////////////////////////////////////////
    // JsonAdapter
    ///////////////////////////////////////////////////////////////////////////
    fun setJsonAdapter(jsonAdapter: IJsonAdapter) {
        this.jsonAdapter = jsonAdapter
    }

    fun getJsonAdapter(): IJsonAdapter {
        if (jsonAdapter == null) {
            throw IllegalStateException("为了不引入其他的json解析依赖，特地将这部分放出去，必须添加一个对应的 json 解析工具，参考代码 sample/GsonJsonAdapter.java")
        }
        return jsonAdapter!!
    }

    ///////////////////////////////////////////////////////////////////////////
    // RequestAdapter
    ///////////////////////////////////////////////////////////////////////////
    fun setRequestAdapter(requestAdapter: IRequestAdapter) {
        this.requestAdapter = requestAdapter
    }

    fun getRequestAdapter(): IRequestAdapter {
        return if (requestAdapter != null) {
            requestAdapter!!
        } else DefaultRequestAdapter()
    }
}
