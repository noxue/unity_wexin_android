package com.yue.nn.Bean;

/**
 * Created by Star_Spark on 2016/11/23.
 * 授权口令获取失败，解析返回错误信息
 */

public class WXErrorInfo {
    /**
     * errcode : 40030
     * errmsg : invalid refresh_token
     */

    private int errcode;
    private String errmsg;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
