package net.ssignal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDate {

    private Date 日期;

    public MyDate(long 毫秒) {
        日期 = new Date(毫秒);
    }

    public MyDate(Date 日期) {
        this.日期 = 日期;
    }

    public String 年() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("yyyy");
        return 简单日期格式.format(日期);
    }

    public String 月() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("MM");
        return 简单日期格式.format(日期);
    }

    public String 日() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("dd");
        return 简单日期格式.format(日期);
    }

    public String 小时() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("HH");
        return 简单日期格式.format(日期);
    }

    public String 分钟() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("mm");
        return 简单日期格式.format(日期);
    }

    public String 秒() {
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat("ss");
        return 简单日期格式.format(日期);
    }

    public String 获取(String 格式字符串) {
        MyDate 当前UTC时间 = new MyDate(SharedMethod.获取当前UTC时间());
        SimpleDateFormat 简单日期格式 = new SimpleDateFormat(格式字符串);
        return 简单日期格式.format(日期);
    }

}
