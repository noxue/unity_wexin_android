package com.yue.nn;

import android.os.Bundle;
import com.yue.nn.wxapi.AppConst;
import com.yue.nn.wxapi.WXEntryActivity;
import com.unity3d.player.UnityPlayerActivity;


public class MainActivity extends UnityPlayerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void RegisterToWeChat(String appId)
    {
        AppConst.WEIXIN_APP_ID = appId;
        WXEntryActivity.initWeiXin(this, appId);
    }

    //微信登录
    public void weiLogin() {
        WXEntryActivity.loginWeixin();
    }

}
