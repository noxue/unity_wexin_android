# 介绍

实现unity游戏安卓版微信登录

# 用法

* 把本项目包名改成跟你自己unity项目的player settings中一样。

* 修改 `com.yue.nn.wxapi.AppConst` 中 `GOBJECT_NAME`为unity项目要接收微信通知code信息的脚本所挂载Object的名称

* 然后在挂载的脚本中把unity项目调用下方代码注册到微信。

```
#if UNITY_IPHONE
#elif UNITY_ANDROID
        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
        jo.Call("RegisterToWeChat", Config.WeChatAppId);
#endif
```

* 然后调用下方代码唤起微信，获取code

```
#if UNITY_IPHONE
#elif UNITY_ANDROID
    jo.Call("weiLogin");
#endif
```

* ** 注意 ** 为了要唤起微信，必须要保证在开放平台后台填写的应用签名是当前应用的签名，下面链接中页面最后有签名获取工具，和我们的应用安装在同一个手机上，输入我们游戏的包名就可以获取到签名了，然后把签名更新到开放平台后台即可。

https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419319167&token=5a178024e1c7e6bd19b9bce970aa6c2de570477f&lang=zh_CN


* 然后在c#脚本中编写接收code的代码即可，注意ResCode函数名是固定的（这个函数名是在`com.yue.nn.wxapi.WXEntryActivity.onResp`函数中指定的，如果要改，可以自己改，只要两个地方相同即可）

```
public void ResCode(string code)
{
    Api.User.LoginByWeChatCode(code);
}
```
