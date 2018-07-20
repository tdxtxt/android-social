package com.fungo.socialgo.share.weixin

/**
 * @author Pinger
 * @since 18-7-20 下午4:02
 *
 */

object WxApi {

    private const val WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token"   //获取access token
    private const  val WX_GET_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo"   //获取用户信息

    interface Callback {
        fun onComplete(data: Map<String, String>)

        fun onError(msg: String)
    }


    /**
     * 获取access_token
     *
     * @param wxAppId     wx appid
     * @param wxAppSecret wx appsecret
     * @param code        调用微信登录获取的code
     * @param callback
     */
    fun getAccessToken(wxAppId: String, wxAppSecret: String,
                       code: String,
                       callback: Callback) {
        val access_token_url = (WX_ACCESS_TOKEN_URL + "?appid=" + wxAppId + "&secret="
                + wxAppSecret + "&code=" + code + "&grant_type=authorization_code")

        //获取access token
        //        NetUtils.doGet(access_token_url, new NetUtils.HttpResponseCallBack() {
        //            @Override
        //            public void onSuccess(JSONObject response) {
        //                if (response == null || response.length() == 0) {
        //                    callback.onError("null respone");
        //                    return;
        //                }
        //
        //                if(response.optString("access_token") == null || response.optString("access_token").length() == 0) {
        //                    callback.onError("errcode=" + response.optString("errcode") + " errmsg=" + response.optString("errmsg"));
        //                    return;
        //                }
        //
        //                Map<String, String> data = new HashMap<String, String>();
        //                String[] keys = {"access_token", "expires_in", "refresh_token", "openid", "scope"};
        //                for(int i=0; i<keys.length; i++) {
        //                    data.put(keys[i], response.optString(keys[i]));
        //                }
        //
        //                callback.onComplete(data);
        //            }
        //
        //            @Override
        //            public void onFailure() {
        //                callback.onError("error net");
        //            }
        //        });
    }


    /**
     * 获取用户信息
     *
     * @param openid       openid
     * @param access_token access_token
     * @param callback
     */
    fun getUserInfo(openid: String, access_token: String,
                    callback: Callback) {
        val get_user_info_url = (WX_GET_USER_INFO_URL + "?access_token=" + access_token
                + "&openid=" + openid)

        //获取userinfo
        //        NetUtils.doGet(get_user_info_url, new NetUtils.HttpResponseCallBack() {
        //            @Override
        //            public void onSuccess(JSONObject response) {
        //                if (response == null || response.length() == 0) {
        //                    callback.onError("null respone");
        //                    return;
        //                }
        //
        //                if(response.optString("openid") == null || response.optString("openid").length() == 0) {
        //                    callback.onError("errcode=" + response.optString("errcode") + " errmsg=" + response.optString("errmsg"));
        //                    return;
        //                }
        //
        //
        //                Map<String, String> data = new HashMap<String, String>();
        //                String[] keys = {"openid", "nickname", "sex", "province", "city", "country", "headimgurl", "unionid"};
        //                for(int i=0; i<keys.length; i++) {
        //                    data.put(keys[i], response.optString(keys[i]));
        //                }
        //
        //                callback.onComplete(data);
        //            }
        //
        //            @Override
        //            public void onFailure() {
        //                callback.onError("error net");
        //            }
        //        });
    }
}