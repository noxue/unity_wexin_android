package com.yue.nn.wxapi;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.yue.nn.MainActivity;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import my.WeChatController;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String WEIXIN_ACCESS_TOKEN_KEY = "wx_access_token_key";
    private static final String WEIXIN_OPENID_KEY = "wx_openid_key";
    private static final String WEIXIN_REFRESH_TOKEN_KEY = "wx_refresh_token_key";

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    public static IWXAPI api;
    public static Context mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        // 微信事件回调接口注册
        //MyApplication.sApi.handleIntent(getIntent(), this);
        api = WXAPIFactory.createWXAPI(this, WeChatController.APP_ID);
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
        req.transaction = WeChatController.Transaction.RequestLogin;
        /**
         * 用于保持请求和回调的状态，授权请求后原样带回给第三方
         * 为了防止csrf攻击（跨站请求伪造攻击），后期改为随机数加session来校验
         */
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }

    public static void SendReq(BaseReq req)
    {
        api.sendReq(req);
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }*/

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        JSONObject json = new JSONObject();
        try{
            json.put("errCode",resp.errCode + "    22222222222");
            json.put("transaction", resp.transaction);
            switch (resp.transaction)
            {
                case WeChatController.Transaction.RequestLogin:
                    /*SendAuth.Resp auth = (SendAuth.Resp)resp;
                    json.put("code", auth.code + "      11111111");

                    json.put("userName", auth.userName);
                    json.put("state", auth.state);
                    json.put("resultUrl", auth.resultUrl);
                    json.put("token", auth.token);*/
                    break;
                case WeChatController.Transaction.ShareImage:
                case WeChatController.Transaction.ShareMusic:
                case WeChatController.Transaction.ShareText:
                case WeChatController.Transaction.ShareUrl:
                case WeChatController.Transaction.ShareVideo:

                    break;
            }
            switch (resp.errCode) {
                // 发送成功
                case BaseResp.ErrCode.ERR_OK:
                    // 获取code
                    String code = ((SendAuth.Resp) resp).code;
                    MainActivity.Instance.getResponse(code);
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

        finish();///
    }

    public void OnRespAuth(SendAuth.Resp resp)
    {
        //String msg = resp.userName+","+resp.state+","+resp.errCode + "," + resp.resultUrl;
//        UnityPlayer.UnitySendMessage("CallBackCube","WeixinLoginCallBack",msg);
        UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", "msg");
    }

    private void goToGetMsg() {
//        Intent intent = new Intent(this, GetFromWXActivity.class);
//        intent.putExtras(getIntent());
//        startActivity(intent);
//        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

        StringBuffer msg = new StringBuffer(); //
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);

        finish();
    }

}