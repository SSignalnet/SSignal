package net.ssignal.language;

import android.content.Context;

import net.ssignal.util.SharedMethod;

public class Chinese {//简繁转换

    public static Chinese 简繁转换器;

    private String 简体字集, 繁体字集;

    public Chinese(Context 场景) {
        简体字集 = SharedMethod.获取附属文本文件(场景, "CN_J");
        繁体字集 = SharedMethod.获取附属文本文件(场景, "CN_F");
    }

    public String 转换成繁体(String 简体字) {
        StringBuffer 字符串合并器 = new StringBuffer(简体字.length());
        int j;
        for (int i = 0; i < 简体字.length(); i++) {
            char 字符 = 简体字.charAt(i);
            j=简体字集.indexOf(字符);
            if (j<0) {
                字符串合并器.append(字符);
            } else {
                字符串合并器.append(繁体字集.charAt(j));
            }
        }
        return 字符串合并器.toString();
    }

    public String 转换成简体(String 繁体字) {
        StringBuffer 字符串合并器 = new StringBuffer(繁体字.length());
        int j;
        for (int i = 0; i < 繁体字.length(); i++) {
            char 字符 = 繁体字.charAt(i);
            j=繁体字集.indexOf(字符);
            if (j<0) {
                字符串合并器.append(字符);
            } else {
                字符串合并器.append(简体字集.charAt(j));
            }
        }
        return 字符串合并器.toString();
    }

}
