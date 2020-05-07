package net.ssignal.protocols;

import net.ssignal.util.SharedMethod;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import static net.ssignal.util.SharedMethod.字符串未赋值或为空;

public class ProtocolMethods {

    public static long 转换成Java相对时间(long 绝对时间) {
        return (绝对时间 - 621355968000000000L) / 10000;
    }

    public static long 转换成Java相对时间2(long 绝对或相对时间) {
        if (绝对或相对时间 > 621355968000000000L) {
            return (绝对或相对时间 - 621355968000000000L) / 10000;
        } else {
            return 绝对或相对时间;
        }
    }

    public static String 生成大写英文字母与数字的随机字符串(int 长度) {
        if (长度 < 1) {长度=1;}
        byte 字节数组[] = new byte[长度];
        Random 随机数生成器 = new Random(System.currentTimeMillis());
        int 随机数;
        for (int i = 0; i < 长度; i++) {
            随机数 = 随机数生成器.nextInt(62);
            if (随机数 < 10) {
                字节数组[i] = (byte) (随机数生成器.nextInt(10) + 48);
            } else if (随机数 < 36) {
                字节数组[i] = (byte) (随机数生成器.nextInt(26) + 65);
            }
        }
        try {
            return new String(字节数组,"US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static boolean 是否是中文用户名(String 字符串) {
        if (字符串未赋值或为空(字符串)) {
            return false;
        }
        if (字符串.length() < ProtocolParameters.最小值_本国语用户名长度 || 字符串.length() > ProtocolParameters.最大值_本国语用户名长度) {
            return false;
        }
        char 字符数组[] = 字符串.toCharArray();
        final int 下界 = 0x4E00;
        final int 上界 = 0x9FA5;
        int i, k, 汉字数 = 0;
        for (i = 0; i < 字符数组.length; i++) {
            k = 字符数组[i];
            if (k >= 下界 && k <= 上界) {
                汉字数 += 1;
            } else if (k >= 48 && k <= 57) {    //0-9的数字
            } else if (k == 95) {    //下划线
            } else {
                return false;
            }
        }
        if (汉字数 > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean 是否是英文用户名(String 字符串) {
        if (字符串未赋值或为空(字符串)) {
            return false;
        }
        if (字符串.length() < ProtocolParameters.最小值_英语用户名长度 || 字符串.length() > ProtocolParameters.最大值_英语用户名长度) {
            return false;
        }
        char 字符数组[] = 字符串.toCharArray();
        int i, k, 英语小写字母数 = 0;
        for (i = 0; i < 字符数组.length; i++) {
            k = 字符数组[i];
            if (k >= 97 && k <= 122) {
                英语小写字母数 += 1;
            } else if (k >= 48 && k <= 57) {    //0-9的数字
            } else if (k == 95) {    //下划线
            } else {
                return false;
            }
        }
        if (英语小写字母数 > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean 是否是有效的讯宝或电子邮箱地址(String 字符串) {
        if (字符串未赋值或为空(字符串)) {
            return false;
        }
        if (字符串.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
            return false;
        }
        String 段[] = 字符串.split("@");
        if (段.length == 2) {
            if (字符串.contains(" ")) {
                return false;
            }
            if (!字符串.contains(".")) {
                return false;
            }
            String 节[] = 段[1].split("\\.");
            if (节.length < 2) {
                return false;
            } else if (节.length > 5) {
                return false;
            }
            for (int i = 0; i < 节.length; i++) {
                if (字符串未赋值或为空(节[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean 检查英语域名(String 英语域名) {
        if (字符串未赋值或为空(英语域名)) {
            return false;
        }
        if (英语域名.length() > ProtocolParameters.最大值_域名长度) {
            return false;
        }
        String 段[] = 英语域名.split("\\.");
        if (段.length < 2 || 段.length > 3) {
            return false;
        }
        int i;
        for (i = 0; i < 段.length; i++) {
            if (SharedMethod.字符串未赋值或为空(段[i])) {
                return false;
            }
        }
        char 字符数组[] = 段[0].toCharArray();
        int k;
        for (i = 0; i < 字符数组.length; i++) {
            k = 字符数组[i];
            if (k >= 97 && k <= 122) {
            } else if (k >= 48 && k <= 57) {    //0-9的数字
            } else if (k == 95) {    //下划线
            } else if (k == 45) {    //中横线
            } else {
                return false;
            }
        }
        int j;
        for (i = 1; i < 段.length; i++) {
            字符数组 = 段[i].toCharArray();
            for (j = 0; j < 字符数组.length; j++) {
                k = 字符数组[j];
                if (k >= 97 && k <= 122) {
                } else {
                    return false;
                }
            }
        }
        return true;
    }

}
