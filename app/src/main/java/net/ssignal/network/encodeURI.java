package net.ssignal.network;

import net.ssignal.util.SharedMethod;

import java.io.UnsupportedEncodingException;

public class encodeURI {

    private static final String 忽略的字符 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

    public static String 替换URI敏感字符(String URI字符串) {
        if (SharedMethod.字符串未赋值或为空(URI字符串)) {
            return URI字符串;
        }
        StringBuffer 字符串拼接器 = new StringBuffer(URI字符串.length() * 3);
        try {
            for (int i = 0; i < URI字符串.length(); i++) {
                String 某个字符 = URI字符串.substring(i, i + 1);
                if (忽略的字符.indexOf(某个字符) < 0) {
                    字符串拼接器.append(getHex(某个字符.getBytes("UTF-8")));
                } else {
                    字符串拼接器.append(某个字符);
                }
            }
            return 字符串拼接器.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return URI字符串;
    }

    private static String getHex(byte 字节数组[]) {
        StringBuffer 字符串拼接器 = new StringBuffer(字节数组.length * 3);
        for (int i = 0; i < 字节数组.length; i++) {
            int n = (int) 字节数组[i] & 0xff;
            字符串拼接器.append("%");
            if (n < 0x10) {
                字符串拼接器.append("0");
            }
            字符串拼接器.append(Long.toString(n, 16).toUpperCase());
        }
        return 字符串拼接器.toString();
    }
}
