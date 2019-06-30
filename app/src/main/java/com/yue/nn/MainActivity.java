package com.yue.nn;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yue.nn.Bean.WXAccessTokenInfo;
import com.yue.nn.Bean.WXErrorInfo;
import com.yue.nn.Bean.WXUserInfo;
import com.yue.nn.wxapi.AppConst;
import com.yue.nn.wxapi.WXEntryActivity;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import my.Util;
import my.WeChatController;

public class MainActivity extends UnityPlayerActivity {

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
    private static final String WEIXIN_ACCESS_TOKEN_KEY = "wx_access_token_key";
    private static final String WEIXIN_OPENID_KEY = "wx_openid_key";
    private static final String WEIXIN_REFRESH_TOKEN_KEY = "wx_refresh_token_key";

    public static final int Get_TOKEN = 0;
    public static final int Refresh_TOKEN = 1;
    public static final int Get_UserInfo = 2;
    public static final int Check_TOKEN = 3;

    String accessToken = "";
    String openid = "";

    public static MainActivity Instance;
    Context mContext = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        mContext = this;
    }

    public void RegisterToWeChat(String appId,String appSecret)
    {
        AppConst.WEIXIN_APP_ID = appId;
        AppConst.WEIXIN_APP_SECRET = appSecret;
        WeChatController.GetInstance().RegisterToWeChat(this, appId);
        WXEntryActivity.initWeiXin(this, appId);
    }
    public void StartAc(String appId)
    {
        Toast.makeText(MainActivity.Instance, "////////////", Toast.LENGTH_SHORT).show();
    }
    public void WeChat(String param)
    {
        try {
            JSONObject jsonObject = new JSONObject(param);
            int _type =  jsonObject.getInt("type");
            WeChatController con = WeChatController.GetInstance();
            switch (_type)
            {
                case WeChatController.Type.WeiChatInterfaceType_IsWeiChatInstalled:
                    break;
                case WeChatController.Type.WeiChatInterfaceType_RequestLogin:
                    //con.WeChatLogin();
                    weiLogin();
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareUrl:
                    ShareLinkUrl(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareImage:
                    ShareImage(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareText:
                    ShareText(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareVideo:
                    ShareVideo(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareMusic:
                    ShareMusic(jsonObject);
                    break;
            }
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", e.toString());
        }

    }

    //微信登录
    public void weiLogin() {
        WXEntryActivity.loginWeixin();
        /*// 从手机本地获取存储的授权口令信息，判断是否存在access_token
        accessToken = (String) ShareUtils.getValue(this, WEIXIN_ACCESS_TOKEN_KEY,
                "none");
        openid = (String) ShareUtils.getValue(this, WEIXIN_OPENID_KEY, "");
        if (!"none".equals(accessToken)) {
            // 有access_token，判断是否过期有效
            isExpireAccessToken(accessToken, openid);
        } else {
            // 没有access_token
        WXEntryActivity.loginWeixin();
        }*/
    }


    public void getResponse(String code) {
        // 通过code获取授权口令access_token
        getAccessToken(code);
        /*// 从手机本地获取存储的授权口令信息，判断是否存在access_token，不存在请求获取，存在就判断是否过期
        accessToken = (String) ShareUtils.getValue(this, WEIXIN_ACCESS_TOKEN_KEY,
                "none");
        openid = (String) ShareUtils.getValue(this, WEIXIN_OPENID_KEY, "");
        if (!"none".equals(accessToken)) {
            // 有access_token，判断是否过期有效
            isExpireAccessToken(accessToken, openid);
        } else {
            // 没有access_token
            getAccessToken(code);
        }*/
    }

    /**
     * 微信登录获取授权口令
     */
    private void getAccessToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + AppConst.WEIXIN_APP_ID +
                "&secret=" + AppConst.WEIXIN_APP_SECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        sendRequestWithHttpClient(url,Get_TOKEN);
    }

    /**
     * 微信登录处理获取的授权信息结果
     *
     * @param response 授权信息结果
     */
    public void processGetAccessTokenResult(String response) {
        UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", response);
        Gson mGson = new Gson();
        // 验证获取授权口令返回的信息是否成功
        if (validateSuccess(response)) {
            // 使用Gson解析返回的授权口令信息
            WXAccessTokenInfo tokenInfo = mGson.fromJson(response, WXAccessTokenInfo.class);
            // 保存信息到手机本地
            saveAccessInfotoLocation(tokenInfo);
            // 获取用户信息
            getUserInfo(tokenInfo.getAccess_token(), tokenInfo.getOpenid());
        } else {
            // 授权口令获取失败，解析返回错误信息
            WXErrorInfo wxErrorInfo = mGson.fromJson(response, WXErrorInfo.class);

        }
    }

    /**
     *微信登录获取tokenInfo的WEIXIN_OPENID_KEY，WEIXIN_ACCESS_TOKEN_KEY，WEIXIN_REFRESH_TOKEN_KEY保存到shareprephence中
     * @param tokenInfo
     */
    private void saveAccessInfotoLocation(WXAccessTokenInfo tokenInfo) {
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_OPENID_KEY,tokenInfo.getOpenid());
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_ACCESS_TOKEN_KEY,tokenInfo.getAccess_token());
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_REFRESH_TOKEN_KEY,tokenInfo.getRefresh_token());
    }

    /**
     * 验证是否成功
     *
     * @param response 返回消息
     * @return 是否成功
     */
    private boolean validateSuccess(String response) {
        String errFlag = "errmsg";
        return (errFlag.contains(response) && "ok".equals(response))
                || (!errFlag.contains(response));
    }


    /**
     * 微信登录判断accesstoken是过期
     *
     * @param accessToken token
     * @param openid      授权用户唯一标识
     */
    private void isExpireAccessToken(final String accessToken, final String openid) {
        String url = "https://api.weixin.qq.com/sns/auth?" +
                "access_token=" + accessToken +
                "&openid=" + openid;
        sendRequestWithHttpClient(url,Check_TOKEN);
    }
    /**
     * 微信登录刷新获取新的access_token
     */
    private void refreshAccessToken() {
        // 从本地获取以存储的refresh_token
        final String refreshToken = (String) ShareUtils.getValue(this, WEIXIN_REFRESH_TOKEN_KEY,
                "");
        if (TextUtils.isEmpty(refreshToken)) {
            return;
        }
        // 拼装刷新access_token的url请求地址
        String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
                "appid=" + AppConst.WEIXIN_APP_ID +
                "&grant_type=refresh_token" +
                "&refresh_token=" + refreshToken;
        sendRequestWithHttpClient(url,Refresh_TOKEN);
    }
    /**
     * 微信token验证成功后，联网获取用户信息
     * @param access_token
     * @param openid
     */
    private void getUserInfo(String access_token, String openid) {
        final Gson mGson = new Gson();
        String url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + access_token +
                "&openid=" + openid;
        sendRequestWithHttpClient(url,Get_UserInfo);
    }


    //新建Handler的对象，在这里接收Message
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String response = "";
            switch (msg.what) {
                case Get_TOKEN:
                    response = (String) msg.obj;
                    processGetAccessTokenResult(response);
                    break;
                case Get_UserInfo:
                    response = (String) msg.obj;
                    final Gson mGson = new Gson();
                    // 解析获取的用户信息
                    WXUserInfo userInfo =  mGson.fromJson(response, WXUserInfo.class);
                    UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", response);
                    UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "UserInfo", response);
                    break;
                case Check_TOKEN:
                    if (validateSuccess(response)) {
                        // accessToken没有过期，获取用户信息
                        getUserInfo(accessToken, openid);

                    } else {
                        // 过期了，使用refresh_token来刷新accesstoken
                        refreshAccessToken();
                    }
                    break;
                case Refresh_TOKEN:
                    if (validateSuccess(response)) {
                        processGetAccessTokenResult(response);

                    } else {
                        // 过期了
                        WXEntryActivity.loginWeixin();
                    }

                    break;
                default:
                    break;
            }
        }

    };

    //方法：发送网络请求，获取url的数据。在里面开启线程
    private void sendRequestWithHttpClient(final String url, final int state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //用HttpClient发送请求，分为五步
                //第一步：创建HttpClient对象
                HttpClient httpCient = new DefaultHttpClient();
                //第二步：创建代表请求的对象,参数是访问的服务器地址
                HttpGet httpGet = new HttpGet(url);

                try {
                    //第三步：执行请求，获取服务器发还的相应对象
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串
                        //在子线程中将Message对象发出去
                        Message message = new Message();
                        message.what = state;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", e.getMessage());
                }

            }
        }).start();
    }


    //分享文字
    public void ShareText(JSONObject jsonObject) {
        String description = "";
        String text = "";
        boolean isCircleOfFriends = false;
        try {
            description = jsonObject.getString("description");
            text = jsonObject.getString("text");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
//         msg.title = "Will be ignored";
        msg.description = description;
        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WeChatController.Transaction.ShareText; // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        // 调用api接口发送数据到微信
        WXEntryActivity.SendReq(req);
    }

    public void ShareImage (JSONObject jsonObject) {
        boolean isCircleOfFriends = false;
        try {
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // 设置消息的缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareImage;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareVideo (JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }

        WXVideoObject video = new WXVideoObject();
        video.videoUrl = url;

        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;
        msg.mediaObject = video;

        // 设置消息的缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareVideo;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareMusic (JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = "url";

        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;

        msg.mediaObject = music;

        // 设置消息的缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareMusic;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareLinkUrl(JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage(AppConst.GOBJECT_NAME, "CallBack", "异常");
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        //用WXMebpageObject 对象初始化一个WXMediaMessage对象，填写标题，描述

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;  //描述只在发送给朋友时显示，发送到朋友圈不显示
        //链接图片
        Resources re = MainActivity.Instance.getResources();  //通过一个活动的Activity  (UnityPlayerActivity._instance)提换为可用的Activity

        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//        int id = re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName());
//        if (id == 0 )
//        {
//            Toast.makeText(MainActivity.Instance, "et app_icon fail ", Toast.LENGTH_SHORT).show();
//        }else
//        {
//            Bitmap thumb = BitmapFactory.decodeResource(re,id); //图片小于32k
//            msg.thumbData = Util.bmpToByteArray(thumb, true);
//        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WeChatController.Transaction.ShareUrl;
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        WXEntryActivity.SendReq(req);
    }

}
