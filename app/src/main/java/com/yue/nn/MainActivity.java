package com.yue.nn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
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


    public void shareImage(String path) {
        // 初始化一个WXImageObject对象
        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(path);
        // 用WXImageObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // 图片压缩
        Bitmap bmp = BitmapFactory.decodeFile(path);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();

        // 封装发送信息
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        WXEntryActivity.api.sendReq(req);
    }

    public void shareText(String text) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        //用 WXTextObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        // 封装发送信息
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        WXEntryActivity.api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}

