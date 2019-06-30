package my;

import android.util.Log;

/**
 * Created by Administrator on 2016/9/6 0006.
 */
public class Interface {
    static public void InterFaceUnity(String msg)
    {
        Log.i("sv111",msg);
//        WeChatController.GetInstance().SendToWei("111");
//        WeChatController.GetInstance().ShareLinkUrl();
        WeChatController.GetInstance().WeChatLogin();
    }
}