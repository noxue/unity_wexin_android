package com.yue.nn.wxapi;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unity3d.player.UnityPlayer;
import org.json.JSONException;
import org.json.JSONObject;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    public static IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 微信事件回调接口注册
        api = WXAPIFactory.createWXAPI(this, AppConst.WEIXIN_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    //微信组件注册初始化
    public static IWXAPI initWeiXin(Context context, String weixin_app_id) {
        if (TextUtils.isEmpty(weixin_app_id)) {
            Toast.makeText(context.getApplicationContext(), "app_id 不能为空", Toast.LENGTH_SHORT).show();
        }
        api = WXAPIFactory.createWXAPI(context, weixin_app_id, true);
        api.registerApp(weixin_app_id);
        return api;
    }

    /**
     * 登录微信
     */
    public static void loginWeixin() {
        // 发送授权登录信息，来获取code
        SendAuth.Req req = new SendAuth.Req();
        // 应用的作用域，获取个人信息
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_niuniu";
        api.sendReq(req);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        JSONObject json = new JSONObject();
        try{
            json.put("errCode",resp.errCode + "    22222222222");
            switch (resp.errCode) {
                // 发送成功
                case BaseResp.ErrCode.ERR_OK:
                    // 获取code
                    String code = ((SendAuth.Resp) resp).code;
                   // MainActivity.Instance.getResponse(code);
                    json.put("code", code + "      11111111");
                    UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "ResCode", code);  // 把code告诉unity
                    break;
            }
        }catch (Exception e)
        {
            try {
                json.put("exception",e.getMessage());
            } catch (JSONException e1) {
            }
        }
        UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", json.toString());

        finish();
    }
}