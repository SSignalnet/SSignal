package net.ssignal.network;

import net.ssignal.LargeChatGroupServer;
import net.ssignal.util.Constants;

public class httpSetting {

    private String 路径;
    private byte 字节数组[];
    private int 收发时限_毫秒;
    public LargeChatGroupServer 大聊天群服务器;

    public httpSetting(String 路径) {
        this.路径 = 路径;
        this.收发时限_毫秒 = Constants.收发时限;
    }

    public httpSetting(String 路径, int 收发时限_毫秒) {
        this.路径 = 路径;
        if (收发时限_毫秒 < Constants.收发时限) {
            this.收发时限_毫秒 = Constants.收发时限;
        } else {
            this.收发时限_毫秒 = 收发时限_毫秒;
        }
    }

    public httpSetting(String 路径, int 收发时限_毫秒, byte 字节数组[]) {
        this.路径 = 路径;
        if (收发时限_毫秒 < Constants.收发时限) {
            this.收发时限_毫秒 = Constants.收发时限;
        } else {
            this.收发时限_毫秒 = 收发时限_毫秒;
        }
        this.字节数组 = 字节数组;
    }

    public httpSetting(String 路径, byte 字节数组[]) {
        this.路径 = 路径;
        this.收发时限_毫秒 = Constants.收发时限;
        this.字节数组 = 字节数组;
    }

    public httpSetting(String 路径, int 收发时限_毫秒, byte 字节数组[], LargeChatGroupServer 大聊天群服务器1) {
        this.路径 = 路径;
        if (收发时限_毫秒 < Constants.收发时限) {
            this.收发时限_毫秒 = Constants.收发时限;
        } else {
            this.收发时限_毫秒 = 收发时限_毫秒;
        }
        this.字节数组 = 字节数组;
        this.大聊天群服务器 = 大聊天群服务器1;
    }

    public String 获取路径() {
        return 路径;
    }

    public int 获取收发时限() {
        return 收发时限_毫秒;
    }

    public byte[] 获取附加数据() {
        return 字节数组;
    }

}
